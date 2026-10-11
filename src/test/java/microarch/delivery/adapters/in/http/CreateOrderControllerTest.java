package microarch.delivery.adapters.in.http;

import libs.errs.Result;
import microarch.delivery.adapters.in.http.model.Address;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateOrderControllerTest {

    private final CreateOrderCommandHandler createOrderCommandHandler = mock(CreateOrderCommandHandler.class);
    private final CreateOrderController controller = new CreateOrderController(createOrderCommandHandler);

    @Test
    void createsOrderThroughUseCase() {
        UUID orderId = UUID.randomUUID();
        when(createOrderCommandHandler.handle(any())).thenReturn(Result.success(orderId));
        NewOrder request = new NewOrder(orderId, new Address("Russia", "Moscow", "Tverskaya", "1", "2"), 3);

        var response = controller.createOrder(request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOrderId()).isEqualTo(orderId);
        verify(createOrderCommandHandler).handle(any());
    }
}
