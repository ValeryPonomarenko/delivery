package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateOrderApi;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreateOrderController implements CreateOrderApi {

    private final CreateOrderCommandHandler createOrderCommandHandler;

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(NewOrder newOrder) {
        if (newOrder == null || newOrder.getAddress() == null || newOrder.getVolume() == null) {
            return ResponseEntity.badRequest().build();
        }

        var commandResult = CreateOrderCommand.create(newOrder.getId(), newOrder.getAddress().getCountry(),
                newOrder.getAddress().getCity(), newOrder.getAddress().getStreet(), newOrder.getAddress().getHouse(),
                newOrder.getAddress().getApartment(), newOrder.getVolume());
        if (commandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var handleResult = createOrderCommandHandler.handle(commandResult.getValue());
        if (handleResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var response = new CreateOrderResponse();
        response.setOrderId(handleResult.getValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
