package microarch.delivery;

import libs.ddd.Aggregate;
import libs.ddd.DomainEvent;
import libs.ddd.DomainEventPublisher;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.stereotype.Component;

@Component
public class DefaultDomainEventPublisher implements DomainEventPublisher {
    private final DomainEventProducer domainEventProducer;

    public DefaultDomainEventPublisher(DomainEventProducer domainEventProducer) {
        this.domainEventProducer = domainEventProducer;
    }

    @Override
    public void publish(Iterable<? extends Aggregate<?>> aggregates) {
        for (Aggregate<?> aggregate : aggregates) {
            for (DomainEvent event : aggregate.getDomainEvents()) {
                domainEventProducer.produce(event);
            }
            aggregate.clearDomainEvents();
        }
    }
}
