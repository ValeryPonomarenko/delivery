package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private final OrderRepository orderRepository;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateOrderCommand command) {
        Location location = new Location(
                ThreadLocalRandom.current().nextInt(Location.MIN_COORDINATE, Location.MAX_COORDINATE + 1),
                ThreadLocalRandom.current().nextInt(Location.MIN_COORDINATE, Location.MAX_COORDINATE + 1));
        Order order = new Order(command.getOrderId(), location, new Volume(command.getVolume()));

        orderRepository.save(order);
        return Result.success(order.getId());
    }
}
