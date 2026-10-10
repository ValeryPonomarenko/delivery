package microarch.delivery.core.application.commands;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CreateOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CreateOrderCommandHandler handler = new CreateOrderCommandHandlerImpl(orderRepository);

    @Test
    void createsAndSavesOrder() {
        UUID orderId = UUID.randomUUID();
        CreateOrderCommand command = CreateOrderCommand.create(
                orderId, "Russia", "Moscow", "Tverskaya", "1", "2", 3).getValue();

        var result = handler.handle(command);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrder.capture());
        assertThat(result.getValue()).isEqualTo(orderId);
        assertThat(savedOrder.getValue().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedOrder.getValue().getVolume().getValue()).isEqualTo(3);
    }
}
