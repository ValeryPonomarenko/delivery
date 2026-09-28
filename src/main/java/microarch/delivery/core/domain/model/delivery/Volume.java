package microarch.delivery.core.domain.model.delivery;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;

import java.util.List;

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
