package no.fintlabs.provider.register;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.AdapterContract;
import no.fintlabs.provider.security.AdapterContractContext;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegistrationService {

    private final AdapterContractProducer adapterContractProducer;
    private final AdapterRegistrationTopicService adapterRegistrationTopicService;
    private final AdapterContractContext adapterContractContext;

    public void register(AdapterContract adapterContract) {
        adapterRegistrationTopicService.ensureTopics(adapterContract);
        adapterContractProducer.send(adapterContract, adapterContract.getOrgId());
        adapterContractContext.add(adapterContract.getAdapterId(), adapterContract.getUsername());
        log.info("Adapter registered {}", adapterContract);
    }

}
