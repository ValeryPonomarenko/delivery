package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final GeoClient geoClient;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository, GeoClient geoClient) {
        this.orderRepository = orderRepository;
        this.geoClient = geoClient;
    }

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateOrderCommand command) {
        var locationResult = geoClient.getLocation(command.getStreet());
        if (locationResult.isFailure()) {
            return Result.failure(locationResult.getError());
        }

        var volumeResult = Volume.create(command.getVolume());
        if (volumeResult.isFailure()) {
            return Result.failure(volumeResult.getError());
        }

        var orderResult = Order.create(command.getOrderId(), locationResult.getValue(), volumeResult.getValue());
        if (orderResult.isFailure()) {
            return Result.failure(orderResult.getError());
        }

        orderRepository.save(orderResult.getValue());
        return Result.success(orderResult.getValue().getId());
    }
}
