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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourierTest {

    @Test
    void takesOrderWhenTotalVolumeDoesNotExceedTwentyLitres() {
        Courier courier = new Courier("Alex", new Location(1, 1));
        Order order = new Order(UUID.randomUUID(), new Location(2, 1), new Volume(20));

        Assignment assignment = courier.takeOrder(order);

        assertThat(courier.canTakeOrder(new Volume(1))).isFalse();
        assertThat(courier.getAssignments()).containsExactly(assignment);
        assertThat(assignment.getOrderId()).isEqualTo(order.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void cannotTakeOrderThatExceedsRemainingCapacity() {
        Courier courier = new Courier("Alex", new Location(1, 1));
        courier.takeOrder(new Order(UUID.randomUUID(), new Location(2, 1), new Volume(15)));

        assertThatThrownBy(() -> courier.takeOrder(
                new Order(UUID.randomUUID(), new Location(3, 1), new Volume(6))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completesAssignmentOnlyWhenCourierIsAtMostOneStepAway() {
        Courier courier = new Courier("Alex", new Location(1, 1));
        Assignment assignment = courier.takeOrder(
                new Order(UUID.randomUUID(), new Location(2, 1), new Volume(1)));

        courier.completeAssignment(assignment.getId());

        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    @Test
    void doesNotCompleteAssignmentWhenCourierIsMoreThanOneStepAway() {
        Courier courier = new Courier("Alex", new Location(1, 1));
        Assignment assignment = courier.takeOrder(
                new Order(UUID.randomUUID(), new Location(3, 1), new Volume(1)));

        assertThatThrownBy(() -> courier.completeAssignment(assignment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    @Test
    void movesOnlyToSameOrNeighbouringLocation() {
        Courier courier = new Courier("Alex", new Location(1, 1));

        courier.moveTo(new Location(1, 2));

        assertThat(courier.getLocation()).isEqualTo(new Location(1, 2));
        assertThatThrownBy(() -> courier.moveTo(new Location(3, 2)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
