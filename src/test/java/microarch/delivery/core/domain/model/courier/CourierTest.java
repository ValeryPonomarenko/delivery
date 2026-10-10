package microarch.delivery.core.domain.model.courier;

import microarch.delivery.core.domain.model.delivery.Assignment;
import microarch.delivery.core.domain.model.delivery.AssignmentStatus;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourierTest {

    @Test
    void takesOrderWhenTotalVolumeDoesNotExceedTwentyLitres() {
        Courier courier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(2, 1), Volume.mustCreate(20)).getValueOrThrow();

        Assignment assignment = courier.takeOrder(order).getValueOrThrow();

        assertThat(courier.canTakeOrder(Volume.mustCreate(1))).isFalse();
        assertThat(courier.getAssignments()).containsExactly(assignment);
        assertThat(assignment.getOrderId()).isEqualTo(order.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void cannotTakeOrderThatExceedsRemainingCapacity() {
        Courier courier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();
        courier.takeOrder(Order.create(UUID.randomUUID(), Location.mustCreate(2, 1), Volume.mustCreate(15)).getValueOrThrow());

        var result = courier.takeOrder(
                Order.create(UUID.randomUUID(), Location.mustCreate(3, 1), Volume.mustCreate(6)).getValueOrThrow());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.capacity.exceeded");
    }

    @Test
    void completesAssignmentOnlyWhenCourierIsAtMostOneStepAway() {
        Courier courier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();
        Assignment assignment = courier.takeOrder(
                Order.create(UUID.randomUUID(), Location.mustCreate(2, 1), Volume.mustCreate(1)).getValueOrThrow()).getValueOrThrow();

        assertThat(courier.completeAssignment(assignment.getId()).isSuccess()).isTrue();

        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void doesNotCompleteAssignmentWhenCourierIsMoreThanOneStepAway() {
        Courier courier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();
        Assignment assignment = courier.takeOrder(
                Order.create(UUID.randomUUID(), Location.mustCreate(3, 1), Volume.mustCreate(1)).getValueOrThrow()).getValueOrThrow();

        var result = courier.completeAssignment(assignment.getId());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.is.too.far.away");
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void movesOnlyToSameOrNeighbouringLocation() {
        Courier courier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();

        assertThat(courier.moveTo(Location.mustCreate(1, 2)).isSuccess()).isTrue();

        assertThat(courier.getLocation()).isEqualTo(Location.mustCreate(1, 2));
        assertThat(courier.moveTo(Location.mustCreate(3, 2)).isFailure()).isTrue();
    }
}
