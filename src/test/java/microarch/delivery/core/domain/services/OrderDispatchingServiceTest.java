package microarch.delivery.core.domain.services;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDispatchingServiceTest {

    private final OrderDispatchingService service = new OrderDispatchingServiceImpl();

    @Test
    void assignsOrderToNearestCourierWithAvailableCapacity() {
        Order order = new Order(UUID.randomUUID(), new Location(5, 5), new Volume(4));
        Courier distantCourier = new Courier("Alex", new Location(1, 1));
        Courier nearestCourier = new Courier("Maria", new Location(4, 5));

        var result = service.dispatch(order, List.of(distantCourier, nearestCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(nearestCourier);
        assertThat(nearestCourier.getAssignments()).hasSize(1);
        assertThat(distantCourier.getAssignments()).isEmpty();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void skipsFullCourierAndAssignsOrderToNextSuitableCourier() {
        Courier fullCourier = new Courier("Alex", new Location(5, 4));
        fullCourier.takeOrder(new Order(UUID.randomUUID(), new Location(5, 4), new Volume(20)));
        Courier availableCourier = new Courier("Maria", new Location(5, 3));
        Order order = new Order(UUID.randomUUID(), new Location(5, 5), new Volume(1));

        var result = service.dispatch(order, List.of(fullCourier, availableCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(availableCourier);
    }

    @Test
    void returnsBusinessErrorWhenNoCourierCanTakeOrder() {
        Courier fullCourier = new Courier("Alex", new Location(5, 4));
        fullCourier.takeOrder(new Order(UUID.randomUUID(), new Location(5, 4), new Volume(20)));
        Order order = new Order(UUID.randomUUID(), new Location(5, 5), new Volume(1));

        var result = service.dispatch(order, List.of(fullCourier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.not.available");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void returnsBusinessErrorForOrderThatIsAlreadyAssigned() {
        Order order = new Order(UUID.randomUUID(), new Location(5, 5), new Volume(1));
        order.assign();

        var result = service.dispatch(order, List.of(new Courier("Alex", new Location(5, 4))));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("order.cannot.be.dispatched");
    }
}
