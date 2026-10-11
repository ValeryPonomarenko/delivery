package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.MoveCourierApi;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.commands.MoveCourierCommand;
import microarch.delivery.core.application.commands.MoveCourierCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MoveCourierController implements MoveCourierApi {

    private final MoveCourierCommandHandler moveCourierCommandHandler;

    @Override
    public ResponseEntity<Void> moveCourier(UUID courierId, Location location) {
        if (location == null || location.getX() == null || location.getY() == null) {
            return ResponseEntity.badRequest().build();
        }

        var locationResult = microarch.delivery.core.domain.model.delivery.Location.create(location.getX(),
                location.getY());
        if (locationResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var commandResult = MoveCourierCommand.create(courierId, locationResult.getValue());
        if (commandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var handleResult = moveCourierCommandHandler.handle(commandResult.getValue());
        if (handleResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }
}
