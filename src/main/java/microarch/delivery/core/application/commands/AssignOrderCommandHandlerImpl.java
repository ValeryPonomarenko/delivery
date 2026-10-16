package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.services.OrderDispatchingService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderDispatchingService orderDispatchingService;
    private final DomainEventPublisher domainEventPublisher;

    public AssignOrderCommandHandlerImpl(OrderRepository orderRepository, CourierRepository courierRepository,
            OrderDispatchingService orderDispatchingService, DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.orderDispatchingService = orderDispatchingService;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrderCommand command) {
        var order = orderRepository.findAnyCreated();
        if (order.isEmpty()) {
            return UnitResult.failure(Error.of("order.not.available", "No order with CREATED status is available"));
        }

        var dispatchResult = orderDispatchingService.dispatch(order.get(), courierRepository.findAll());
        if (dispatchResult.isFailure()) {
            return UnitResult.failure(dispatchResult.getError());
        }

        orderRepository.save(order.get());
        courierRepository.save(dispatchResult.getValue());
        domainEventPublisher.publish(List.of(dispatchResult.getValue(), order.get()));
        return UnitResult.success();
    }
}
