package danielpm.dev.redislinkapi.integration;

import danielpm.dev.redislinkapi.repository.UrlRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * @author danielpm.dev
 */
@SpringBootTest
@DisplayName("Tests de integración con Redis")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisabledOnOs(value = OS.WINDOWS, disabledReason = "Redis embebido tiene problemas de memoria en Windows")
public class RedisIntegrationTest {

    private RedisServer redisServer;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redis;

    @BeforeAll
    void startRedis() throws IOException {
        redisServer = new RedisServer(6370); // Puerto diferente al default
        redisServer.start();
    }

    @AfterAll
    void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @BeforeEach
    void cleanRedis() {
        Assertions.assertNotNull(redis.getConnectionFactory());
        redis.getConnectionFactory().getConnection();
    }

    @Test
    @DisplayName("Debe guardar y recuperar una URL correctamente")
    void shouldSaveAndRetrieveUrl() {
        // Given
        String shortCode = "happy-blue-tiger";
        String longUrl = "https://www.example.com";

        // When
        urlRepository.saveUrl(shortCode, longUrl);
        Optional<String> retrieved = urlRepository.findLongUrlByShortCode(shortCode);

        // Then
        assertThat(retrieved)
                .isPresent()
                .contains(longUrl);
    }

    @Test
    @DisplayName("Debe incrementar hits correctamente")
    void shouldIncrementHits() {
        // Given
        String shortCode = "swift-red-panda";
        String longUrl = "https://www.example.com";
        urlRepository.saveUrl(shortCode, longUrl);

        // When
        Long hits1 = urlRepository.incrementHits(shortCode);
        Long hits2 = urlRepository.incrementHits(shortCode);
        Long hits3 = urlRepository.incrementHits(shortCode);

        // Then
        assertThat(hits1).isEqualTo(1);
        assertThat(hits2).isEqualTo(2);
        assertThat(hits3).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe guardar y recuperar metadatos completos")
    void shouldSaveAndRetrieveMetadata() {
        // Given
        String shortCode = "cool-gold-eagle";
        String longUrl = "https://www.example.com";
        urlRepository.saveUrl(shortCode, longUrl);

        // When
        Optional<Map<String, String>> metadata = urlRepository.findMetadata(shortCode);

        // Then
        assertThat(metadata)
                .isPresent()
                .get()
                .satisfies(map -> {
                    assertThat(map).containsEntry("longUrl", longUrl);
                    assertThat(map).containsEntry("shortCode", shortCode);
                    assertThat(map).containsEntry("hits", "0");
                    assertThat(map).containsKey("createdAt");
                });
    }

    @Test
    @DisplayName("Debe buscar shortCode por URL larga")
    void shouldFindShortCodeByLongUrl() {
        // Given
        String shortCode = "brave-purple-wolf";
        String longUrl = "https://www.example.com/test";
        urlRepository.saveUrl(shortCode, longUrl);

        // When
        Optional<String> found = urlRepository.findShortCodeByLongUrl(longUrl);

        // Then
        assertThat(found)
                .isPresent()
                .contains(shortCode);
    }

    @Test
    @DisplayName("Debe verificar existencia de código corto")
    void shouldCheckExistence() {
        // Given
        String shortCode = "quick-cyan-lion";
        String longUrl = "https://www.example.com";
        urlRepository.saveUrl(shortCode, longUrl);

        // When & Then
        assertThat(urlRepository.existsByShortCode(shortCode)).isTrue();
        assertThat(urlRepository.existsByShortCode("non-existent")).isFalse();
    }

    @Test
    @DisplayName("Debe eliminar URL y metadatos completamente")
    void shouldDeleteCompletely() {
        // Given
        String shortCode = "wild-amber-shark";
        String longUrl = "https://www.example.com";
        urlRepository.saveUrl(shortCode, longUrl);

        // When
        urlRepository.delete(shortCode);

        // Then
        assertThat(urlRepository.findLongUrlByShortCode(shortCode)).isEmpty();
        assertThat(urlRepository.findMetadata(shortCode)).isEmpty();
        assertThat(urlRepository.existsByShortCode(shortCode)).isFalse();
    }

    @Test
    @DisplayName("Debe guardar URL con TTL y expirar")
    void shouldSaveWithTTLAndExpire() throws InterruptedException {
        // Given
        String shortCode = "temp-link";
        String longUrl = "https://www.example.com";
        long ttlSeconds = 2;

        // When
        urlRepository.saveUrlWithTTL(shortCode, longUrl, ttlSeconds);

        // Verificar que existe inicialmente
        assertThat(urlRepository.existsByShortCode(shortCode)).isTrue();

        // Esperar a que expire
        Thread.sleep(3000);

        // Then
        assertThat(urlRepository.existsByShortCode(shortCode)).isFalse();
    }

    @Test
    @DisplayName("Debe manejar múltiples URLs simultáneamente")
    void shouldHandleMultipleUrls() {
        // Given
        String[] shortCodes = {"code1", "code2", "code3"};
        String[] longUrls = {
                "https://example1.com",
                "https://example2.com",
                "https://example3.com"
        };

        // When
        for (int i = 0; i < shortCodes.length; i++) {
            urlRepository.saveUrl(shortCodes[i], longUrls[i]);
        }

        // Then
        for (int i = 0; i < shortCodes.length; i++) {
            Optional<String> retrieved = urlRepository.findLongUrlByShortCode(shortCodes[i]);
            assertThat(retrieved).isPresent().contains(longUrls[i]);
        }
    }
}
