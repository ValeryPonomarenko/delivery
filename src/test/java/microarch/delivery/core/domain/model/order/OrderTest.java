package microarch.delivery.core.domain.model.order;

import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void createsOrderInCreatedStatusAndAllowsValidTransitions() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(2, 6), Volume.mustCreate(4)).getValueOrThrow();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.assign().isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(order.complete().isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void rejectsInvalidStatusTransitions() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(2, 6), Volume.mustCreate(4)).getValueOrThrow();

        assertThat(order.complete().isFailure()).isTrue();
        assertThat(order.assign().isSuccess()).isTrue();
        assertThat(order.assign().isFailure()).isTrue();
    }
}
