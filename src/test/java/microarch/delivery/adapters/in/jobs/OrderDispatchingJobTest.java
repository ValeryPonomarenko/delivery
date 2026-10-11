package microarch.delivery.adapters.in.jobs;

import libs.errs.UnitResult;
import microarch.delivery.core.application.commands.AssignOrderCommandHandler;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderDispatchingJobTest {

    @Test
    void dispatchesOneOrderThroughUseCase() {
        AssignOrderCommandHandler assignOrderCommandHandler = mock(AssignOrderCommandHandler.class);
        when(assignOrderCommandHandler.handle(any())).thenReturn(UnitResult.success());
        OrderDispatchingJob job = new OrderDispatchingJob(assignOrderCommandHandler);

        job.dispatchOrder();

        verify(assignOrderCommandHandler).handle(any());
    }
}
