package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class GetNotCompletedOrdersQueryHandlerImpl implements GetNotCompletedOrdersQueryHandler {

    private final OrderRepository orderRepository;

    public GetNotCompletedOrdersQueryHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Result<GetNotCompletedOrdersResponse, Error> handle(GetNotCompletedOrdersQuery query) {
        var orders = orderRepository.findAllNotCompleted().stream()
                .map(order -> new OrderDto(
                        order.getId(),
                        new LocationDto(order.getLocation().getX(), order.getLocation().getY())))
                .toList();
        return Result.success(new GetNotCompletedOrdersResponse(orders));
    }
}
