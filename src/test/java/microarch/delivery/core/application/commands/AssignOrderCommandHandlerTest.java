package microarch.delivery.core.application.commands;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.services.OrderDispatchingServiceImpl;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final AssignOrderCommandHandler handler = new AssignOrderCommandHandlerImpl(
            orderRepository, courierRepository, new OrderDispatchingServiceImpl());

    @Test
    void assignsCreatedOrderAndSavesBothAggregates() {
        Order order = Order.create(UUID.randomUUID(), Location.mustCreate(2, 1), Volume.mustCreate(2)).getValueOrThrow();
        Courier courier = Courier.create("Alex", Location.mustCreate(1, 1)).getValueOrThrow();
        when(orderRepository.findAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.findAll()).thenReturn(List.of(courier));

        var result = handler.handle(AssignOrderCommand.create());

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(courier.getAssignments()).hasSize(1);
        verify(orderRepository).save(order);
        verify(courierRepository).save(courier);
    }
}
