package sh.kamath.url_service.dto;


import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortenRequest(
        @URL
        @NotBlank(message = "URL is required")
        String url)
{
}
