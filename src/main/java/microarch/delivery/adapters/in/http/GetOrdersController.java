package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetOrdersApi;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQuery;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetOrdersController implements GetOrdersApi {

    private final GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler;

    @Override
    public ResponseEntity<List<Order>> getOrders() {
        var result = getNotCompletedOrdersQueryHandler.handle(GetNotCompletedOrdersQuery.create());
        if (result.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var orders = result.getValue().orders().stream().map(orderDto -> {
            var location = new Location();
            location.setX(orderDto.location().x());
            location.setY(orderDto.location().y());
            var order = new Order();
            order.setId(orderDto.id());
            order.setLocation(location);
            return order;
        }).toList();
        return ResponseEntity.ok(orders);
    }
}
