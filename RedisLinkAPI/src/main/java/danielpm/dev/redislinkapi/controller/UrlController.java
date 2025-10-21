package danielpm.dev.redislinkapi.controller;

import danielpm.dev.redislinkapi.model.dto.LinkDto;
import danielpm.dev.redislinkapi.model.dto.ShortenRequest;
import danielpm.dev.redislinkapi.model.dto.ShortenUrlResponse;
import danielpm.dev.redislinkapi.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * @author danielpm.dev
 */
@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService urlService;

    @Value("${app-base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }


    /**
     * GET /api/v1/urls/{shortCode}
     * Redirects the user from the short URL to the original long URL, and increments the hit counter for that link.
     * @return {void} Returns a permanent redirection (HTTP 301). The 'Location' header contains the original long URL.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToUrl(@PathVariable String shortCode) {
        String longUrl = urlService.getLongUrlAndIncrementHits(shortCode);

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(longUrl))
                .build();
    }


    /**
     * POST /api/v1/urls/short
     * Creates a short URL for a given long URL.
     * @return {ShortenUrlResponse} A response object with the newly created short URL, its short code, and link details.
     */
    @PostMapping("/short")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        String shortCode = urlService.createShortUrl(request.url());

        LinkDto link = urlService.getLinkByShortCode(shortCode);

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .successful(true)
                .message("Link successfully created")
                .link(link)
                .shortUrl(buildShortUrl(shortCode))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/urls/{shortCode}/stats
     * Get statistics and details for a short URL using its short code.
     * @return {LinkDto} A response object containing the link's details and statistics (e.g., creation date, original URL, and click count).
     */
    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<LinkDto> getStats(@PathVariable String shortCode) {
        LinkDto link = urlService.getLinkByShortCode(shortCode);
        return ResponseEntity.ok(link);
    }

    private String buildShortUrl(String shortCode) {
        return baseUrl + "/api/v1/urls/" + shortCode;
    }
}
