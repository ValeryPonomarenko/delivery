package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateOrderCommand {

    private final UUID orderId;
    private final String country;
    private final String city;
    private final String street;
    private final String house;
    private final String apartment;
    private final int volume;

    public static Result<CreateOrderCommand, Error> create(UUID orderId, String country, String city,
            String street, String house, String apartment, int volume) {
        Error error = Guard.combine(
                Guard.againstNullOrEmpty(orderId, "orderId"),
                Guard.againstNullOrEmpty(country, "country"),
                Guard.againstNullOrEmpty(city, "city"),
                Guard.againstNullOrEmpty(street, "street"),
                Guard.againstNullOrEmpty(house, "house"),
                Guard.againstNullOrEmpty(apartment, "apartment"),
                Guard.againstLessOrEqual(volume, 0, "volume"));
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new CreateOrderCommand(orderId, country, city, street, house, apartment, volume));
    }
}
