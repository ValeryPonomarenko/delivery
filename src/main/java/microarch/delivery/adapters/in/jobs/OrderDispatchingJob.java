package microarch.delivery.adapters.in.jobs;

import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.commands.AssignOrderCommand;
import microarch.delivery.core.application.commands.AssignOrderCommandHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDispatchingJob {

    private final AssignOrderCommandHandler assignOrderCommandHandler;

    @Scheduled(fixedDelay = 1000)
    public void dispatchOrder() {
        assignOrderCommandHandler.handle(AssignOrderCommand.create());
    }
}
