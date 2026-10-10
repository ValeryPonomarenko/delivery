package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private OrderRepository repository;

    @Test
    void storesAndFindsOrdersByIdAndStatus() {
        Order created = Order.create(UUID.randomUUID(), Location.mustCreate(1, 1), Volume.mustCreate(2)).getValueOrThrow();
        Order assigned = Order.create(UUID.randomUUID(), Location.mustCreate(2, 2), Volume.mustCreate(3)).getValueOrThrow();
        assigned.assign();

        repository.save(created);
        repository.save(assigned);

        assertThat(repository.findById(created.getId())).contains(created);
        assertThat(repository.findAnyCreated()).contains(created);
        assertThat(repository.findAllAssigned()).containsExactly(assigned);
    }

    @Test
    void updatesOrderStatus() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(1, 1), Volume.mustCreate(2)).getValueOrThrow();
        repository.save(order);
        order.assign();

        repository.save(order);

        assertThat(repository.findById(order.getId()))
                .get()
                .extracting(Order::getStatus)
                .isEqualTo(OrderStatus.ASSIGNED);
    }
}
