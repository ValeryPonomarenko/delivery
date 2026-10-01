package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class OrderDispatchingServiceImpl implements OrderDispatchingService {

    @Override
    public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(couriers, "couriers must not be null");
        couriers.forEach(courier -> Objects.requireNonNull(courier, "courier must not be null"));

        if (order.getStatus() != OrderStatus.CREATED) {
            return Result.failure(Errors.orderCannotBeDispatched());
        }

        return couriers.stream()
                .filter(courier -> courier.canTakeOrder(order.getVolume()))
                .min(Comparator.comparingInt(courier -> courier.getLocation().distanceTo(order.getLocation())))
                .map(winner -> {
                    winner.takeOrder(order);
                    order.assign();
                    return Result.<Courier, Error>success(winner);
                })
                .orElseGet(() -> Result.failure(Errors.courierNotAvailable()));
    }

    private static final class Errors {
        static Error orderCannotBeDispatched() {
            return Error.of("order.cannot.be.dispatched", "Only an order with CREATED status can be dispatched");
        }

        static Error courierNotAvailable() {
            return Error.of("courier.not.available", "No courier with enough remaining capacity is available");
        }
    }
}
