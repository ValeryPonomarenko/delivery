package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import microarch.delivery.core.domain.model.delivery.Location;

import java.util.Objects;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoveCourierCommand {

    private final UUID courierId;
    private final Location location;

    public static Result<MoveCourierCommand, Error> create(UUID courierId, Location location) {
        Error error = Guard.againstNullOrEmpty(courierId, "courierId");
        if (error != null) {
            return Result.failure(error);
        }
        if (Objects.isNull(location)) {
            return Result.failure(GeneralErrors.valueIsRequired("location"));
        }

        return Result.success(new MoveCourierCommand(courierId, location));
    }
}
