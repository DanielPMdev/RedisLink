package danielpm.dev.redislinkapi.repository;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * @author danielpm.dev
 */
@Repository
public class UrlRepository {

    private static final String URL_KEY_PREFIX = "short:";
    private static final String META_KEY_PREFIX = "meta:";
    private static final String REVERSE_LOOKUP_PREFIX = "long:";

    private final StringRedisTemplate redis;
    private final HashOperations<String, String, String> hashOps;

    public UrlRepository(StringRedisTemplate redis) {
        this.redis = redis;
        this.hashOps = redis.opsForHash();
    }

    /**
     * Save the original URL associated with the shortcode
     */
    public void saveUrl(String shortCode, String longUrl) {
        String urlKey = URL_KEY_PREFIX + shortCode;
        String metaKey = META_KEY_PREFIX + shortCode;
        String reverseLookupKey = REVERSE_LOOKUP_PREFIX + hashLongUrl(longUrl);

        redis.opsForValue().set(urlKey, longUrl);
        redis.opsForValue().set(reverseLookupKey, shortCode); // Para búsqueda inversa

        hashOps.put(metaKey, "longUrl", longUrl);
        hashOps.put(metaKey, "shortCode", shortCode);
        hashOps.put(metaKey, "createdAt", LocalDateTime.now().toString());
        hashOps.put(metaKey, "hits", "0");
    }

    /**
     * Save with TTL (time to live)
     */
    public void saveUrlWithTTL(String shortCode, String longUrl, long seconds) {
        String urlKey = URL_KEY_PREFIX + shortCode;
        String metaKey = META_KEY_PREFIX + shortCode;
        String reverseLookupKey = REVERSE_LOOKUP_PREFIX + hashLongUrl(longUrl);

        Duration ttl = Duration.ofSeconds(seconds);

        redis.opsForValue().set(urlKey, longUrl, ttl);
        redis.opsForValue().set(reverseLookupKey, shortCode, ttl);

        hashOps.put(metaKey, "longUrl", longUrl);
        hashOps.put(metaKey, "shortCode", shortCode);
        hashOps.put(metaKey, "createdAt", LocalDateTime.now().toString());
        hashOps.put(metaKey, "hits", "0");
        hashOps.put(metaKey, "ttl", String.valueOf(seconds));

        redis.expire(metaKey, ttl);
    }

    /**
     * Restore the original URL
     */
    public Optional<String> findLongUrlByShortCode(String shortCode) {
        String longUrl = redis.opsForValue().get(URL_KEY_PREFIX + shortCode);
        return Optional.ofNullable(longUrl);
    }

    /**
     * Search for the shortCode associated with a longUrl (reverse search)
     */
    public Optional<String> findShortCodeByLongUrl(String longUrl) {
        String shortCode = redis.opsForValue().get(REVERSE_LOOKUP_PREFIX + hashLongUrl(longUrl));
        return Optional.ofNullable(shortCode);
    }

    /**
     * Increase the click counter atomically
     */
    public Long incrementHits(String shortCode) {
         return hashOps.increment(META_KEY_PREFIX + shortCode, "hits", 1);
    }

    /**
     * Get all metadata for a shortCode
     */
    public Optional<Map<String, String>> findMetadata(String shortCode) {
        Map<String, String> metadata = hashOps.entries(META_KEY_PREFIX + shortCode);
        return metadata.isEmpty() ? Optional.empty() : Optional.of(metadata);
    }

    /**
     * Get the number of hits
     */
    public Integer getHits(String shortCode) {
        String hits = hashOps.get(META_KEY_PREFIX + shortCode, "hits");
        return hits != null ? Integer.parseInt(hits) : 0;
    }

    /**
     * Check if a shortCode exists
     */
    public boolean existsByShortCode(String shortCode) {
        String key = URL_KEY_PREFIX + shortCode;
        return redis.hasKey(key);
    }

    /**
     * Delete a URL and its metadata
     */
    public void delete(String shortCode) {
        String longUrl = redis.opsForValue().get(URL_KEY_PREFIX + shortCode);

        redis.delete(URL_KEY_PREFIX + shortCode);
        redis.delete(META_KEY_PREFIX + shortCode);

        if (longUrl != null) {
            redis.delete(REVERSE_LOOKUP_PREFIX + hashLongUrl(longUrl));
        }
    }

    /**
     * Generate a hash of the long URL to use as a reverse lookup key.
     */
    private String hashLongUrl(String longUrl) {
        return String.valueOf(longUrl.hashCode());
    }
}

