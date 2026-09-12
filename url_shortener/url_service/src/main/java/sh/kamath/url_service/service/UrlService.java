package sh.kamath.url_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import sh.kamath.url_service.entity.ShortUrl;
import sh.kamath.url_service.event.UrlClickedEvent;
import sh.kamath.url_service.repository.UrlRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    @Value("${app.node-id}")
    private String nodeId;

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "url.clicked";


    private static final String CACHE_KEY_PREFIX = "url:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Transactional
    public ShortUrl shorten(String url) {

        Long nextId = urlRepository.fetchNextId();
        String code = shortCodeGenerator.generateShortCode(nextId);
        ShortUrl shortUrl = new ShortUrl();
            shortUrl.setId(nextId);
            shortUrl.setCode(code);
            shortUrl.setOriginalUrl(url);

        return urlRepository.save(shortUrl);
    }

    public Optional<String> resolve(String code) {
        long startTime = System.currentTimeMillis();
        String cacheKey = CACHE_KEY_PREFIX + code;
        boolean wasInCache;
        Optional<String> resolvedUrl;

        // 1. Check Redis
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
        if (cachedUrl != null) {
            log.debug("Cache HIT for code: {}", code);
            wasInCache = true;
            resolvedUrl = Optional.of(cachedUrl);
        } else {
            log.debug("Cache MISS for code: {}", code);
            wasInCache = false;
            resolvedUrl = urlRepository.findByCode(code)
                    .map(ShortUrl::getOriginalUrl);
            resolvedUrl.ifPresent(url ->
                    redisTemplate.opsForValue().set(cacheKey, url, CACHE_TTL));
        }

        // 2. Fire analytics event (only if URL was found — no analytics for 404s per your spec)
        long resolveTimeMs = System.currentTimeMillis() - startTime;
        resolvedUrl.ifPresent(url -> publishClickEvent(code, url, wasInCache, resolveTimeMs));

        return resolvedUrl;
    }

    private void publishClickEvent(String code, String originalUrl, boolean wasInCache, long resolveTimeMs) {
        UrlClickedEvent event = new UrlClickedEvent(
                code,
                originalUrl,
                Instant.now(),
                null,  // ipAddress - would need HttpServletRequest, deferred
                null,  // userAgent - same
                null,  // referrer - same
                nodeId,
                wasInCache,
                resolveTimeMs
        );

        kafkaTemplate.send(TOPIC, code, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish url.clicked event for code {}: {}", code, ex.getMessage());
                    } else {
                        log.debug("Published url.clicked event for code: {}", code);
                    }
                });
    }
}
