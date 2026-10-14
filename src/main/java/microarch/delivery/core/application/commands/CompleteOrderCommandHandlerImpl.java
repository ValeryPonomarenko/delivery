package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository,
            DomainEventPublisher domainEventPublisher) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var courier = courierRepository.findById(command.getCourierId());
        if (courier.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        }

        var order = orderRepository.findById(command.getOrderId());
        if (order.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("order", command.getOrderId()));
        }

        var assignmentCompleted = courier.get().completeAssignmentForOrder(order.get().getId());
        if (assignmentCompleted.isFailure()) {
            return assignmentCompleted;
        }

        var orderCompleted = order.get().complete();
        if (orderCompleted.isFailure()) {
            return orderCompleted;
        }

        courierRepository.save(courier.get());
        orderRepository.save(order.get());
        domainEventPublisher.publish(List.of(courier.get(), order.get()));
        return UnitResult.success();
    }
}
