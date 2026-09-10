package sh.kamath.url_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import sh.kamath.url_service.entity.ShortUrl;
import sh.kamath.url_service.repository.UrlRepository;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final StringRedisTemplate redisTemplate;

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

    public String resolve(String code) {
        String cacheKey = CACHE_KEY_PREFIX + code;

        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if(cachedUrl != null){
            log.debug("Cache HIT for code: {}", cacheKey);
            return cachedUrl;
        }

        log.debug("Cache Miss for code: {}",cacheKey);

        if(urlRepository.findByCode(code).isPresent()){
            String resolvedUrl = urlRepository.findByCode(code).get().getOriginalUrl();
            redisTemplate.opsForValue().set(cacheKey,resolvedUrl,CACHE_TTL);
            return resolvedUrl;
        }
        else return null;
    }
}
