package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.GeneralErrors;
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
        if (order == null) {
            return Result.failure(GeneralErrors.valueIsRequired("order"));
        }
        if (couriers == null || couriers.stream().anyMatch(Objects::isNull)) {
            return Result.failure(GeneralErrors.valueIsRequired("couriers"));
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            return Result.failure(Errors.orderCannotBeDispatched());
        }

        var winner = couriers.stream()
                .filter(courier -> courier.canTakeOrder(order.getVolume()))
                .min(Comparator.comparingInt(courier -> courier.getLocation().distanceTo(order.getLocation())));
        if (winner.isEmpty()) {
            return Result.failure(Errors.courierNotAvailable());
        }

        var assignment = winner.get().takeOrder(order);
        if (assignment.isFailure()) {
            return Result.failure(assignment.getError());
        }

        var orderAssigned = order.assign();
        if (orderAssigned.isFailure()) {
            return Result.failure(orderAssigned.getError());
        }

        return Result.success(winner.get());
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
