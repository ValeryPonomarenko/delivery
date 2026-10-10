package microarch.delivery.core.application.commands;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.delivery.Assignment;
import microarch.delivery.core.domain.model.delivery.AssignmentStatus;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteOrderCommandHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CompleteOrderCommandHandler handler = new CompleteOrderCommandHandlerImpl(courierRepository, orderRepository);

    @Test
    void completesOrderAndCourierAssignment() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(2, 1), Volume.mustCreate(2)).getValueOrThrow();
        order.assign();
        Courier courier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();
        Assignment assignment = courier.takeOrder(order).getValueOrThrow();
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        CompleteOrderCommand command = CompleteOrderCommand.create(courier.getId(), order.getId()).getValue();

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(courierRepository).save(courier);
        verify(orderRepository).save(order);
    }

    @Test
    void returnsBusinessErrorWhenCourierIsTooFarFromOrder() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(3, 1), Volume.mustCreate(2)).getValueOrThrow();
        order.assign();
        Courier courier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();
        courier.takeOrder(order);
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        CompleteOrderCommand command = CompleteOrderCommand.create(courier.getId(), order.getId()).getValue();

        var result = handler.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getCode()).isEqualTo("courier.is.too.far.away");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        verify(courierRepository, never()).save(courier);
        verify(orderRepository, never()).save(order);
    }
}
