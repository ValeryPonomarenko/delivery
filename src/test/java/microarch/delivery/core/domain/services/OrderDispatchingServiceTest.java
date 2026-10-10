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
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(4)).getValueOrThrow();
        Courier distantCourier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();
        Courier nearestCourier = Courier.create("Maria", Location.mustCreate(4, 5)).getValueOrThrow();

        var result = service.dispatch(order, List.of(distantCourier, nearestCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(nearestCourier);
        assertThat(nearestCourier.getAssignments()).hasSize(1);
        assertThat(distantCourier.getAssignments()).isEmpty();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void skipsFullCourierAndAssignsOrderToNextSuitableCourier() {
        Courier fullCourier = Courier.create("Alex", Location.mustCreate(5, 4)).getValueOrThrow();
        fullCourier.takeOrder(Order.create(UUID.randomUUID(), Location.mustCreate(5, 4), Volume.mustCreate(20)).getValueOrThrow());
        Courier availableCourier = Courier.create("Maria", Location.mustCreate(5, 3)).getValueOrThrow();
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(1)).getValueOrThrow();

        var result = service.dispatch(order, List.of(fullCourier, availableCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(availableCourier);
    }

    @Test
    void returnsBusinessErrorWhenNoCourierCanTakeOrder() {
        Courier fullCourier = Courier.create("Alex", Location.mustCreate(5, 4)).getValueOrThrow();
        fullCourier.takeOrder(Order.create(UUID.randomUUID(), Location.mustCreate(5, 4), Volume.mustCreate(20)).getValueOrThrow());
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(1)).getValueOrThrow();

        var result = service.dispatch(order, List.of(fullCourier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.not.available");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void keepsOrderCreatedWhenCourierListIsEmpty() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(1)).getValueOrThrow();

        var result = service.dispatch(order, List.of());

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.not.available");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void returnsBusinessErrorForOrderThatIsAlreadyAssigned() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(1)).getValueOrThrow();
        order.assign();

        var result = service.dispatch(order, List.of(Courier.create("Alex", Location.mustCreate(5, 4)).getValueOrThrow()));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("order.cannot.be.dispatched");
    }
}
