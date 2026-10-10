package microarch.delivery.core.domain.model.delivery;

import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@Embeddable
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Volume extends ValueObject<Volume> {

    private final int value;

    public Volume(int value) {
        Error.throwIf(Guard.againstLessOrEqual(value, 0, "volume"));
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }
}
