package microarch.delivery.core.domain.model.order;

import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.events.OrderAssignedDomainEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedDomainEvent;
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
        assertThat(order.getDomainEvents()).singleElement().isInstanceOf(OrderAssignedDomainEvent.class);
        assertThat(order.complete().isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getDomainEvents()).hasSize(2);
        assertThat(order.getDomainEvents().get(1)).isInstanceOf(OrderCompletedDomainEvent.class);
    }

    @Test
    void rejectsInvalidStatusTransitions() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(2, 6), Volume.mustCreate(4)).getValueOrThrow();

        assertThat(order.complete().isFailure()).isTrue();
        assertThat(order.getDomainEvents()).isEmpty();
        assertThat(order.assign().isSuccess()).isTrue();
        assertThat(order.assign().isFailure()).isTrue();
        assertThat(order.getDomainEvents()).hasSize(1);
    }
}
