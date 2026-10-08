package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import microarch.delivery.core.ports.TransactionManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SpringTransactionManagerIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Test
    void rollsBackChangesToDifferentAggregatesWhenWorkFails() {
        Order order = new Order(UUID.randomUUID(), new Location(1, 1), new Volume(1));
        Courier courier = new Courier("Alex", new Location(1, 1));

        assertThatThrownBy(() -> transactionManager.execute(() -> {
            orderRepository.save(order);
            courierRepository.save(courier);
            throw new IllegalStateException("simulate failure");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(orderRepository.findById(order.getId())).isEmpty();
        assertThat(courierRepository.findById(courier.getId())).isEmpty();
    }
}
