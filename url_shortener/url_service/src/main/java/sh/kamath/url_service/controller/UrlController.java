package sh.kamath.url_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sh.kamath.url_service.dto.ShortenRequest;
import sh.kamath.url_service.dto.ShortenResponse;
import sh.kamath.url_service.entity.ShortUrl;
import sh.kamath.url_service.service.UrlService;

import java.net.URI;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@RequestBody @Valid ShortenRequest request){

        ShortUrl shortenedUrl = urlService.shorten(request.url());

        ShortenResponse response =  new ShortenResponse(shortenedUrl.getCode()
                , baseUrl + "/"+ shortenedUrl.getCode());
        return new ResponseEntity<>( response, HttpStatus.CREATED);

    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> resolve(@PathVariable String code) {
        return urlService.resolve(code)
                .<ResponseEntity<Void>>map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(url))
                        .build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
