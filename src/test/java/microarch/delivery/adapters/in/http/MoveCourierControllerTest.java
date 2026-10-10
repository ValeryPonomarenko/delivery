package microarch.delivery.adapters.in.http;

import libs.errs.UnitResult;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.commands.MoveCourierCommandHandler;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MoveCourierControllerTest {

    private final MoveCourierCommandHandler moveCourierCommandHandler = mock(MoveCourierCommandHandler.class);
    private final MoveCourierController controller = new MoveCourierController(moveCourierCommandHandler);

    @Test
    void movesCourierThroughUseCase() {
        when(moveCourierCommandHandler.handle(any())).thenReturn(UnitResult.success());

        var response = controller.moveCourier(UUID.randomUUID(), new Location(2, 1));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(moveCourierCommandHandler).handle(any());
    }

    @Test
    void rejectsLocationOutsideBoard() {
        var response = controller.moveCourier(UUID.randomUUID(), new Location(0, 1));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
