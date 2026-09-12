package sh.kamath.analytics_service.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import sh.kamath.analytics_service.entity.ClickEvent;
import sh.kamath.analytics_service.event.UrlClickedEvent;
import sh.kamath.analytics_service.repository.ClickEventRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class UrlClickedConsumer {

    private final ClickEventRepository repository;

    @KafkaListener(topics = "url.clicked", groupId = "analytics-service")
    public void handleUrlClicked(UrlClickedEvent event){
        log.debug("Received url.clicked event for code: {}", event.code());

        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setCode(event.code());
        clickEvent.setOriginalUrl(event.originalUrl());
        clickEvent.setIpAddress(event.ipAddress());
        clickEvent.setUserAgent(event.userAgent());
        clickEvent.setReferrer(event.referrer());
        clickEvent.setResolverNodeId(event.resolverNodeId());
        clickEvent.setWasInCache(event.wasInCache());
        clickEvent.setResolveTimeMs((int) event.resolveTimeMs());
        clickEvent.setClickedAt(event.timestamp());

        repository.save(clickEvent);

        log.debug("Persisted click event for code: {}", event.code());
    }
}