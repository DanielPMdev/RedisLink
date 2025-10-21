package danielpm.dev.redislinkapi.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author danielpm.dev
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para UrlRepository")
public class UrlRepositoryTest {
    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private HashOperations<String, String, String> hashOps;

    private UrlRepository repository;

    @BeforeEach
    void setUp() {
        lenient().doReturn(valueOps).when(redis).opsForValue();
        lenient().doReturn(hashOps).when(redis).opsForHash();

        repository = new UrlRepository(redis);
    }

    @Test
    @DisplayName("Debe guardar una URL correctamente")
    void shouldSaveUrl() {
        // Given
        String shortCode = "happy-blue-tiger";
        String longUrl = "https://www.example.com/very/long/url";

        // When
        repository.saveUrl(shortCode, longUrl);

        // Then
        verify(valueOps).set(eq("short:" + shortCode), eq(longUrl));
        verify(valueOps).set(eq("long:" + longUrl.hashCode()), eq(shortCode));
        verify(hashOps).put(eq("meta:" + shortCode), eq("longUrl"), eq(longUrl));
        verify(hashOps).put(eq("meta:" + shortCode), eq("shortCode"), eq(shortCode));
        verify(hashOps).put(eq("meta:" + shortCode), eq("createdAt"), anyString());
        verify(hashOps).put(eq("meta:" + shortCode), eq("hits"), eq("0"));
    }

    @Test
    @DisplayName("Debe guardar una URL con TTL")
    void shouldSaveUrlWithTTL() {
        // Given
        String shortCode = "swift-red-panda";
        String longUrl = "https://www.example.com";
        long ttlSeconds = 3600L;

        // When
        repository.saveUrlWithTTL(shortCode, longUrl, ttlSeconds);

        // Then
        verify(valueOps).set(eq("short:" + shortCode), eq(longUrl), any(Duration.class));
        verify(redis).expire(eq("meta:" + shortCode), any(Duration.class));
        verify(hashOps).put(eq("meta:" + shortCode), eq("ttl"), eq(String.valueOf(ttlSeconds)));
    }

    @Test
    @DisplayName("Debe encontrar una URL larga por código corto")
    void shouldFindLongUrlByShortCode() {
        // Given
        String shortCode = "cool-gold-eagle";
        String expectedUrl = "https://www.example.com";
        when(valueOps.get("short:" + shortCode)).thenReturn(expectedUrl);

        // When
        Optional<String> result = repository.findLongUrlByShortCode(shortCode);

        // Then
        assertThat(result)
                .isPresent()
                .contains(expectedUrl);
        verify(valueOps).get("short:" + shortCode);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty cuando no existe el código corto")
    void shouldReturnEmptyWhenShortCodeNotFound() {
        // Given
        String shortCode = "non-existent-code";
        when(valueOps.get("short:" + shortCode)).thenReturn(null);

        // When
        Optional<String> result = repository.findLongUrlByShortCode(shortCode);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe encontrar un código corto por URL larga")
    void shouldFindShortCodeByLongUrl() {
        // Given
        String longUrl = "https://www.example.com";
        String expectedShortCode = "brave-purple-wolf";
        when(valueOps.get("long:" + longUrl.hashCode())).thenReturn(expectedShortCode);

        // When
        Optional<String> result = repository.findShortCodeByLongUrl(longUrl);

        // Then
        assertThat(result)
                .isPresent()
                .contains(expectedShortCode);
    }

    @Test
    @DisplayName("Debe incrementar hits correctamente")
    void shouldIncrementHits() {
        // Given
        String shortCode = "quick-cyan-lion";
        when(hashOps.increment("meta:" + shortCode, "hits", 1)).thenReturn(5L);

        // When
        Long hits = repository.incrementHits(shortCode);

        // Then
        assertThat(hits).isEqualTo(5L);
        verify(hashOps).increment("meta:" + shortCode, "hits", 1);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty cuando no hay metadatos")
    void shouldReturnEmptyWhenNoMetadata() {
        // Given
        String shortCode = "non-existent";
        when(hashOps.entries("meta:" + shortCode)).thenReturn(new HashMap<>());

        // When
        Optional<Map<String, String>> result = repository.findMetadata(shortCode);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe verificar si existe un código corto")
    void shouldCheckIfShortCodeExists() {
        // Given
        String shortCode = "bold-jade-bear";
        when(redis.hasKey("short:" + shortCode)).thenReturn(true);

        // When
        boolean exists = repository.existsByShortCode(shortCode);

        // Then
        assertThat(exists).isTrue();
        verify(redis).hasKey("short:" + shortCode);
    }

    @Test
    @DisplayName("Debe eliminar una URL y sus metadatos")
    void shouldDeleteUrlAndMetadata() {
        // Given
        String shortCode = "warm-violet-fox";
        String longUrl = "https://www.example.com";
        when(valueOps.get("short:" + shortCode)).thenReturn(longUrl);

        // When
        repository.delete(shortCode);

        // Then
        verify(redis).delete("short:" + shortCode);
        verify(redis).delete("meta:" + shortCode);
        verify(redis).delete("long:" + longUrl.hashCode());
    }

    @Test
    @DisplayName("Debe obtener hits correctamente")
    void shouldGetHits() {
        // Given
        String shortCode = "gentle-coral-deer";
        when(hashOps.get("meta:" + shortCode, "hits")).thenReturn("25");

        // When
        Integer hits = repository.getHits(shortCode);

        // Then
        assertThat(hits).isEqualTo(25);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando no hay hits registrados")
    void shouldReturn0WhenNoHits() {
        // Given
        String shortCode = "new-code";
        when(hashOps.get("meta:" + shortCode, "hits")).thenReturn(null);

        // When
        Integer hits = repository.getHits(shortCode);

        // Then
        assertThat(hits).isZero();
    }
}
