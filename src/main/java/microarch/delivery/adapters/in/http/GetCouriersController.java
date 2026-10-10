package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetCouriersApi;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.queries.GetAllCouriersQuery;
import microarch.delivery.core.application.queries.GetAllCouriersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetCouriersController implements GetCouriersApi {

    private final GetAllCouriersQueryHandler getAllCouriersQueryHandler;

    @Override
    public ResponseEntity<List<Courier>> getCouriers() {
        var result = getAllCouriersQueryHandler.handle(GetAllCouriersQuery.create());
        if (result.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var couriers = result.getValue().couriers().stream().map(courierDto -> {
            var location = new Location();
            location.setX(courierDto.location().x());
            location.setY(courierDto.location().y());
            var courier = new Courier();
            courier.setId(courierDto.id());
            courier.setName(courierDto.name());
            courier.setLocation(location);
            return courier;
        }).toList();
        return ResponseEntity.ok(couriers);
    }
}
