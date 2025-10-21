package danielpm.dev.redislinkapi.service;

import danielpm.dev.redislinkapi.exceptions.ShortCodeGenerationException;
import danielpm.dev.redislinkapi.exceptions.ShortCodeNotFoundException;
import danielpm.dev.redislinkapi.model.dto.LinkDto;
import danielpm.dev.redislinkapi.repository.UrlRepository;
import danielpm.dev.redislinkapi.util.WordBasedShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
@DisplayName("Tests para UrlService")
public class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private WordBasedShortCodeGenerator codeGenerator;

    @InjectMocks
    private UrlServiceImpl urlService;

    @BeforeEach
    void setUp() {
        // Setup común si es necesario
    }

    @Test
    @DisplayName("Debe obtener link por código corto exitosamente")
    void shouldGetLinkByShortCode() {
        // Given
        String shortCode = "happy-blue-tiger";
        String createdAt = LocalDateTime.now().toString();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("longUrl", "https://www.example.com");
        metadata.put("shortCode", shortCode);
        metadata.put("hits", "5");
        metadata.put("createdAt", createdAt);

        when(urlRepository.findMetadata(shortCode)).thenReturn(Optional.of(metadata));

        // When
        LinkDto result = urlService.getLinkByShortCode(shortCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.longUrl()).isEqualTo("https://www.example.com");
        assertThat(result.shortCode()).isEqualTo(shortCode);
        assertThat(result.hits()).isEqualTo(5);
        assertThat(result.createdAt()).isNotNull();  // ← Añadir esta verificación

        verify(urlRepository).findMetadata(shortCode);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el código corto no existe")
    void shouldThrowExceptionWhenShortCodeNotFound() {
        // Given
        String shortCode = "non-existent";
        when(urlRepository.findMetadata(shortCode)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> urlService.getLinkByShortCode(shortCode))
                .isInstanceOf(ShortCodeNotFoundException.class)
                .hasMessageContaining("Short code not found: " + shortCode);
    }

    @Test
    @DisplayName("Debe obtener URL larga e incrementar hits")
    void shouldGetLongUrlAndIncrementHits() {
        // Given
        String shortCode = "swift-red-panda";
        String longUrl = "https://www.example.com";

        when(urlRepository.findLongUrlByShortCode(shortCode)).thenReturn(Optional.of(longUrl));

        // When
        String result = urlService.getLongUrlAndIncrementHits(shortCode);

        // Then
        assertThat(result).isEqualTo(longUrl);
        verify(urlRepository).findLongUrlByShortCode(shortCode);
        verify(urlRepository).incrementHits(shortCode);
    }

    @Test
    @DisplayName("Debe crear una URL corta nueva")
    void shouldCreateNewShortUrl() {
        // Given
        String longUrl = "https://www.example.com/long/url";
        String generatedCode = "cool-gold-eagle";

        when(urlRepository.findShortCodeByLongUrl(longUrl)).thenReturn(Optional.empty());
        when(codeGenerator.generate()).thenReturn(generatedCode);
        when(urlRepository.existsByShortCode(generatedCode)).thenReturn(false);

        // When
        String result = urlService.createShortUrl(longUrl);

        // Then
        assertThat(result).isEqualTo(generatedCode);
        verify(urlRepository).findShortCodeByLongUrl(longUrl);
        verify(codeGenerator).generate();
        verify(urlRepository).saveUrl(generatedCode, longUrl);
    }

    @Test
    @DisplayName("Debe retornar código existente si la URL ya fue acortada")
    void shouldReturnExistingCodeWhenUrlAlreadyShortened() {
        // Given
        String longUrl = "https://www.example.com";
        String existingCode = "brave-purple-wolf";

        when(urlRepository.findShortCodeByLongUrl(longUrl))
                .thenReturn(Optional.of(existingCode));

        // When
        String result = urlService.createShortUrl(longUrl);

        // Then
        assertThat(result).isEqualTo(existingCode);
        verify(urlRepository).findShortCodeByLongUrl(longUrl);
        verify(codeGenerator, never()).generate();
        verify(urlRepository, never()).saveUrl(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe reintentar generación cuando hay colisión")
    void shouldRetryWhenCollisionOccurs() {
        // Given
        String longUrl = "https://www.example.com";
        String firstCode = "used-code";
        String secondCode = "unique-code";

        when(urlRepository.findShortCodeByLongUrl(longUrl)).thenReturn(Optional.empty());
        when(codeGenerator.generate()).thenReturn(firstCode, secondCode);
        when(urlRepository.existsByShortCode(firstCode)).thenReturn(true);
        when(urlRepository.existsByShortCode(secondCode)).thenReturn(false);

        // When
        String result = urlService.createShortUrl(longUrl);

        // Then
        assertThat(result).isEqualTo(secondCode);
        verify(codeGenerator, times(2)).generate();
        verify(urlRepository).saveUrl(secondCode, longUrl);
    }

    @Test
    @DisplayName("Debe usar timestamp después de múltiples colisiones")
    void shouldUseTimestampAfterMultipleCollisions() {
        // Given
        String longUrl = "https://www.example.com";
        String collisionCode = "collision";
        String timestampCode = "code-with-1234";

        when(urlRepository.findShortCodeByLongUrl(longUrl)).thenReturn(Optional.empty());
        when(codeGenerator.generate()).thenReturn(collisionCode);
        when(codeGenerator.generateWithTimestamp()).thenReturn(timestampCode);
        when(urlRepository.existsByShortCode(collisionCode)).thenReturn(true);
        when(urlRepository.existsByShortCode(timestampCode)).thenReturn(false);

        // When
        String result = urlService.createShortUrl(longUrl);

        // Then
        assertThat(result).isEqualTo(timestampCode);
        verify(codeGenerator, times(5)).generate(); // COLLISION_THRESHOLD = 5
        verify(codeGenerator).generateWithTimestamp();
    }

    @Test
    @DisplayName("Debe lanzar excepción después de máximo de intentos")
    void shouldThrowExceptionAfterMaxAttempts() {
        // Given
        String longUrl = "https://www.example.com";

        when(urlRepository.findShortCodeByLongUrl(longUrl)).thenReturn(Optional.empty());
        when(codeGenerator.generate()).thenReturn("code");
        when(codeGenerator.generateWithTimestamp()).thenReturn("timestamp-code");
        when(urlRepository.existsByShortCode(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> urlService.createShortUrl(longUrl))
                .isInstanceOf(ShortCodeGenerationException.class)
                .hasMessageContaining("No se pudo generar un código único");
    }

    @Test
    @DisplayName("Debe crear URL corta con TTL")
    void shouldCreateShortUrlWithTTL() {
        // Given
        String longUrl = "https://www.example.com";
        String generatedCode = "temp-link";
        long ttlSeconds = 3600L;

        when(codeGenerator.generate()).thenReturn(generatedCode);
        when(urlRepository.existsByShortCode(generatedCode)).thenReturn(false);

        // When
        String result = urlService.createShortUrlWithTTL(longUrl, ttlSeconds);

        // Then
        assertThat(result).isEqualTo(generatedCode);
        verify(urlRepository).saveUrlWithTTL(generatedCode, longUrl, ttlSeconds);
    }

    @Test
    @DisplayName("Debe manejar hits igual a cero en metadata")
    void shouldHandleZeroHits() {
        // Given
        String shortCode = "new-link";
        String createdAt = LocalDateTime.now().toString();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("longUrl", "https://www.example.com");
        metadata.put("shortCode", shortCode);
        metadata.put("hits", "0");
        metadata.put("createdAt", createdAt);

        when(urlRepository.findMetadata(shortCode)).thenReturn(Optional.of(metadata));

        // When
        LinkDto result = urlService.getLinkByShortCode(shortCode);

        // Then
        assertThat(result.hits()).isZero();
        assertThat(result.createdAt()).isNotNull();
    }
}
