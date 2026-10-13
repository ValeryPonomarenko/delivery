package microarch.delivery.adapters.in.kafka;

import libs.errs.Result;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import queues.basket.events.BasketEventsProto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BasketEventsConsumerTest {

    private final CreateOrderCommandHandler createOrderCommandHandler = mock(CreateOrderCommandHandler.class);
    private final BasketEventsConsumer consumer = new BasketEventsConsumer(createOrderCommandHandler);

    @Test
    void createsOrderFromBasketConfirmedEvent() {
        var basketId = UUID.randomUUID();
        var event = BasketEventsProto.BasketConfirmedIntegrationEvent.newBuilder()
                .setBasketId(basketId.toString())
                .setAddress(BasketEventsProto.Address.newBuilder()
                        .setCountry("Russia")
                        .setCity("Moscow")
                        .setStreet("Tverskaya")
                        .setHouse("1")
                        .setApartment("2"))
                .setVolume(3)
                .build();
        when(createOrderCommandHandler.handle(org.mockito.ArgumentMatchers.any(CreateOrderCommand.class)))
                .thenReturn(Result.success(basketId));

        consumer.listen(event.toByteArray());

        var commandCaptor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(createOrderCommandHandler).handle(commandCaptor.capture());
        var command = commandCaptor.getValue();
        assertThat(command.getOrderId()).isEqualTo(basketId);
        assertThat(command.getCountry()).isEqualTo("Russia");
        assertThat(command.getCity()).isEqualTo("Moscow");
        assertThat(command.getStreet()).isEqualTo("Tverskaya");
        assertThat(command.getHouse()).isEqualTo("1");
        assertThat(command.getApartment()).isEqualTo("2");
        assertThat(command.getVolume()).isEqualTo(3);
    }
}
