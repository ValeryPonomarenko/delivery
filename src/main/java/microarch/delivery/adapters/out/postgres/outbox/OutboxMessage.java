package microarch.delivery.adapters.out.postgres.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@NoArgsConstructor
@Getter
public class OutboxMessage {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "occurred_on_utc", nullable = false)
    private Instant occurredOnUtc;

    @Column(name = "processed_on_utc")
    private Instant processedOnUtc;

    public OutboxMessage(UUID id, String eventType, String aggregateId, String aggregateType, String payload,
            Instant occurredOnUtc) {
        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.payload = payload;
        this.occurredOnUtc = occurredOnUtc;
    }

    public void markAsProcessed() {
        processedOnUtc = Instant.now();
    }
}
