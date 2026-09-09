package sh.kamath.url_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sh.kamath.url_service.entity.ShortUrl;
import sh.kamath.url_service.repository.UrlRepository;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

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
        if(urlRepository.findByCode(code).isPresent()){
            return urlRepository.findByCode(code).get().getOriginalUrl();
        }
        else return null;
    }
}
