package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.delivery.Location;
import microarch.delivery.core.domain.model.delivery.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourierRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private CourierRepository repository;

    @Test
    void storesAndRestoresCourierAggregateWithAssignments() {
        Courier courier = new Courier("Alex", new Location(1, 1));
        courier.takeOrder(new Order(UUID.randomUUID(), new Location(2, 1), new Volume(2)));

        repository.save(courier);

        assertThat(repository.findById(courier.getId()))
                .get()
                .satisfies(loaded -> {
                    assertThat(loaded.getName()).isEqualTo("Alex");
                    assertThat(loaded.getAssignments()).hasSize(1);
                });
        assertThat(repository.findAll()).extracting(Courier::getId).contains(courier.getId());
    }
}
