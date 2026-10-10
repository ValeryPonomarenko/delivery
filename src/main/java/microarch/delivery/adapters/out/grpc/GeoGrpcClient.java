package microarch.delivery.adapters.out.grpc;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.ApplicationProperties;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.ports.GeoClient;
import org.springframework.stereotype.Component;

@Component
public class GeoGrpcClient implements GeoClient {

    private final ManagedChannel channel;
    private final GeoGrpc.GeoBlockingStub geoBlockingStub;

    public GeoGrpcClient(ApplicationProperties applicationProperties) {
        var geoService = applicationProperties.getGrpc().getGeoService();
        channel = ManagedChannelBuilder.forAddress(geoService.getHost(), geoService.getPort()).usePlaintext().build();
        geoBlockingStub = GeoGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Location, Error> getLocation(String street) {
        var request = GeoProto.GetGeolocationRequest.newBuilder().setStreet(street).build();
        var response = geoBlockingStub.getGeolocation(request);
        return Location.create(response.getLocation().getX(), response.getLocation().getY());
    }

    @PreDestroy
    public void close() {
        channel.shutdown();
    }
}
