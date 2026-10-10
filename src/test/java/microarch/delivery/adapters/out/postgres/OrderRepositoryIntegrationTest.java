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
        Order created = new Order(UUID.randomUUID(), new Location(1, 1), new Volume(2));
        Order assigned = new Order(UUID.randomUUID(), new Location(2, 2), new Volume(3));
        assigned.assign();

        repository.save(created);
        repository.save(assigned);

        assertThat(repository.findById(created.getId())).contains(created);
        assertThat(repository.findAnyCreated()).contains(created);
        assertThat(repository.findAllAssigned()).containsExactly(assigned);
    }

    @Test
    void updatesOrderStatus() {
        Order order = new Order(UUID.randomUUID(), new Location(1, 1), new Volume(2));
        repository.save(order);
        order.assign();

        repository.save(order);

        assertThat(repository.findById(order.getId()))
                .get()
                .extracting(Order::getStatus)
                .isEqualTo(OrderStatus.ASSIGNED);
    }
}
