package microarch.delivery.core.domain.model.delivery;

import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Volume extends ValueObject<Volume> {

    private final int value;

    private Volume(int value) {
        this.value = value;
    }

    public static Result<Volume, Error> create(int value) {
        Error error = Guard.againstLessOrEqual(value, 0, "volume");
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Volume(value));
    }

    public static Volume mustCreate(int value) {
        return create(value).getValueOrThrow();
    }

    public int getValue() {
        return value;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
