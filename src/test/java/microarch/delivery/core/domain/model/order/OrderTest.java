package microarch.delivery.core.domain.model.order;

import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void createsOrderInCreatedStatusAndAllowsValidTransitions() {
        Order order = new Order(UUID.randomUUID(), new Location(2, 6), new Volume(4));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        order.assign();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        order.complete();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void rejectsInvalidStatusTransitions() {
        Order order = new Order(UUID.randomUUID(), new Location(2, 6), new Volume(4));

        assertThatThrownBy(order::complete).isInstanceOf(IllegalStateException.class);
        order.assign();
        assertThatThrownBy(order::assign).isInstanceOf(IllegalStateException.class);
    }
}
