package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateCourierCommand {

    private final String name;

    public static Result<CreateCourierCommand, Error> create(String name) {
        Error error = Guard.againstNullOrEmpty(name, "name");
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new CreateCourierCommand(name));
    }
}
