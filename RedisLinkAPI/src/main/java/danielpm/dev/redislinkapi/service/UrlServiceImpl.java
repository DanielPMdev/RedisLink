package danielpm.dev.redislinkapi.service;

import danielpm.dev.redislinkapi.exceptions.ShortCodeGenerationException;
import danielpm.dev.redislinkapi.exceptions.ShortCodeNotFoundException;
import danielpm.dev.redislinkapi.model.dto.LinkDto;
import danielpm.dev.redislinkapi.repository.UrlRepository;
import danielpm.dev.redislinkapi.util.WordBasedShortCodeGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * @author danielpm.dev
 */
@Service
public class UrlServiceImpl implements UrlService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private static final int COLLISION_THRESHOLD = 5;
    // Después de 5 colisiones, usa sufijo

    private final UrlRepository urlRepository;
    private final WordBasedShortCodeGenerator codeGenerator;

    public UrlServiceImpl(UrlRepository urlRepository, WordBasedShortCodeGenerator codeGenerator) {
        this.urlRepository = urlRepository;
        this.codeGenerator = codeGenerator;
    }

    /**
     * Gets link information by short code
     * @param shortCode The short code identifier
     * @return LinkDto with all link information
     * @throws ShortCodeNotFoundException if the short code doesn't exist
     */
    @Override
    public LinkDto getLinkByShortCode(String shortCode) {
        Map<String, String> metadata = urlRepository.findMetadata(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(
                        "Short code not found: " + shortCode));

        return LinkDto.builder()
                .longUrl(metadata.get("longUrl"))
                .shortCode(metadata.get("shortCode"))
                .hits(Integer.parseInt(metadata.getOrDefault("hits", "0")))
                .createdAt(LocalDateTime.parse(metadata.get("createdAt")))
                .build();
    }

    /**
     * Get the long URL and increment the hit counter
     */
    @Override
    public String getLongUrlAndIncrementHits(String shortCode) {
        String longUrl = urlRepository.findLongUrlByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(
                        "Short code not found: " + shortCode));

        urlRepository.incrementHits(shortCode);

        return longUrl;
    }

    /**
     * Create a shortened URL
     */
    @Override
    public String createShortUrl(String longUrl) {
        // Verificar si ya existe
        Optional<String> existingShortCode = urlRepository.findShortCodeByLongUrl(longUrl);
        if (existingShortCode.isPresent()) {
            return existingShortCode.get();
        }

        String shortCode = generateUniqueShortCode();
        urlRepository.saveUrl(shortCode, longUrl);

        return shortCode;
    }

    /**
     * Create a shortened URL with an expiration time
     */
    @Override
    public String createShortUrlWithTTL(String longUrl, long seconds) {
        String shortCode = generateUniqueShortCode();
        urlRepository.saveUrlWithTTL(shortCode, longUrl, seconds);

        return shortCode;
    }

    /**
     * Generate a unique code with a fallback strategy in case of collisions.
     */
    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;

        // Intentar generar código sin sufijo
        while (attempts < COLLISION_THRESHOLD) {
            shortCode = codeGenerator.generate();

            if (!urlRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }

            attempts++;
        }

        // Si hay muchas colisiones, usar estrategia con timestamp
        attempts = 0;
        while (attempts < MAX_GENERATION_ATTEMPTS) {
            shortCode = codeGenerator.generateWithTimestamp();

            // ✅ CORRECTO - REVISAR
            if (!urlRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }

            attempts++;
        }

        throw new ShortCodeGenerationException(
                "No se pudo generar un código único después de " +
                        (COLLISION_THRESHOLD + MAX_GENERATION_ATTEMPTS) + " intentos"
        );
    }
}
