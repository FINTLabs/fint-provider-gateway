package no.fintlabs.datasync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.SyncPage;
import no.fintlabs.adapter.models.SyncType;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
@Service
public class SyncPageService {
    private final EntityProducerKafka entityProducerKafka;
    private final MetaDataKafkaProducer metaDataKafkaProducer;

    public <T extends SyncPage<Object>> void doSync(T syncPage, String domain, String packageName, String entity) {
        Instant start = Instant.now();
        String eventName = String.format("adapter-%s-sync", syncPage.getSyncType().toString().toLowerCase());

        if (syncPage.getSyncType() == SyncType.DELETE) {
            syncPage.getResources().forEach(syncPageEntry -> syncPageEntry.setResource(null));
        }
        metaDataKafkaProducer.send(syncPage.getMetadata(), syncPage.getMetadata().getOrgId(), eventName);
        sendEntities(syncPage, domain, packageName, entity);

        Duration timeTaken = Duration.between(start, Instant.now());
        logDuration(syncPage.getSyncType(), syncPage.getMetadata().getCorrId(), timeTaken);
    }

    private void sendEntities(SyncPage<Object> page, String domain, String packageName, String entity) {
        page.getResources().forEach(syncPageEntry -> {
            ListenableFuture<SendResult<String, Object>> future = entityProducerKafka.sendEntity(
                    page.getMetadata().getOrgId(),
                    domain,
                    packageName,
                    entity,
                    syncPageEntry
            );

            future.addCallback(
                    result -> log.debug("Entity sent successfully"),
                    error -> log.error("Error sending entity: " + error.getMessage(), error)
            );
        });
    }

    private void logDuration(SyncType syncType, String corrId, Duration timeTaken) {
        log.info("End {} sync ({}). It took {} hours, {} minutes, {} seconds to complete",
                syncType.toString().toLowerCase(),
                corrId,
                timeTaken.toHoursPart(),
                timeTaken.toMinutesPart(),
                timeTaken.toSecondsPart()
        );
    }
}