package no.fintlabs.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.SyncPageMetadata;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.topic.EventTopicService;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class FullSyncProducer extends KafkaEventProducer<SyncPageMetadata> {

    public FullSyncProducer(EventProducerFactory eventProducerFactory, EventTopicService eventTopicService) {
        super(eventProducerFactory, eventTopicService, SyncPageMetadata.class, "adapter-full-sync");
    }

    public ListenableFuture send(SyncPageMetadata syncPageMetadata) {
        return super.send(syncPageMetadata, syncPageMetadata.getOrgId());
    }

    public void sendAndGet(SyncPageMetadata metadata) {
        try {
            this.send(metadata).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
    }

}
