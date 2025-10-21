package danielpm.dev.redislinkapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import danielpm.dev.redislinkapi.exceptions.ShortCodeNotFoundException;
import danielpm.dev.redislinkapi.model.dto.LinkDto;
import danielpm.dev.redislinkapi.model.dto.ShortenRequest;
import danielpm.dev.redislinkapi.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author danielpm.dev
 */
@WebMvcTest(UrlController.class)
@DisplayName("Tests de integración para UrlController")
public class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlService urlService;

    @Test
    @DisplayName("POST /api/v1/urls/short - Debe crear URL corta exitosamente")
    void shouldShortenUrlSuccessfully() throws Exception {
        // Given
        ShortenRequest request = new ShortenRequest("https://www.example.com/very/long/url");
        String shortCode = "happy-blue-tiger";

        LinkDto linkDto = LinkDto.builder()
                .longUrl(request.url())
                .shortCode(shortCode)
                .hits(0)
                .build();

        when(urlService.createShortUrl(request.url())).thenReturn(shortCode);
        when(urlService.getLinkByShortCode(shortCode)).thenReturn(linkDto);

        // When & Then
        mockMvc.perform(post("/api/v1/urls/short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successful").value(true))
                .andExpect(jsonPath("$.message").value("Link successfully created"))
                .andExpect(jsonPath("$.link.long_url").value(request.url()))
                .andExpect(jsonPath("$.link.short").value(shortCode))
                .andExpect(jsonPath("$.link.hits").value(0))
                .andExpect(jsonPath("$.shortUrl").value(containsString(shortCode)));  // ← Cambiar de short_url a shortUrl

        verify(urlService).createShortUrl(request.url());
        verify(urlService).getLinkByShortCode(shortCode);
    }

    @Test
    @DisplayName("POST /api/v1/urls/short - Debe validar URL vacía")
    void shouldValidateEmptyUrl() throws Exception {
        // Given
        ShortenRequest request = new ShortenRequest("");

        // When & Then
        mockMvc.perform(post("/api/v1/urls/short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(urlService, never()).createShortUrl(anyString());
    }

    @Test
    @DisplayName("POST /api/v1/urls/short - Debe validar URL nula")
    void shouldValidateNullUrl() throws Exception {
        // Given
        String json = "{}";

        // When & Then
        mockMvc.perform(post("/api/v1/urls/short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(urlService, never()).createShortUrl(anyString());
    }

    @Test
    @DisplayName("POST /api/v1/urls/short - Debe validar formato de URL")
    void shouldValidateUrlFormat() throws Exception {
        // Given
        ShortenRequest request = new ShortenRequest("not-a-valid-url");

        // When & Then
        mockMvc.perform(post("/api/v1/urls/short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/urls/{shortCode} - Debe redirigir exitosamente")
    void shouldRedirectSuccessfully() throws Exception {
        // Given
        String shortCode = "swift-red-panda";
        String longUrl = "https://www.example.com";

        when(urlService.getLongUrlAndIncrementHits(shortCode)).thenReturn(longUrl);

        // When & Then
        mockMvc.perform(get("/api/v1/urls/" + shortCode))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", longUrl));

        verify(urlService).getLongUrlAndIncrementHits(shortCode);
    }

    @Test
    @DisplayName("GET /api/v1/urls/{shortCode} - Debe retornar 404 cuando no existe")
    void shouldReturn404WhenShortCodeNotFound() throws Exception {
        // Given
        String shortCode = "non-existent";

        when(urlService.getLongUrlAndIncrementHits(shortCode))
                .thenThrow(new ShortCodeNotFoundException("Short code not found: " + shortCode));

        // When & Then
        mockMvc.perform(get("/api/v1/urls/" + shortCode))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/urls/{shortCode}/stats - Debe obtener estadísticas")
    void shouldGetStats() throws Exception {
        // Given
        String shortCode = "cool-gold-eagle";

        LinkDto linkDto = LinkDto.builder()
                .longUrl("https://www.example.com")
                .shortCode(shortCode)
                .hits(42)
                .build();

        when(urlService.getLinkByShortCode(shortCode)).thenReturn(linkDto);

        // When & Then
        mockMvc.perform(get("/api/v1/urls/" + shortCode + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.long_url").value("https://www.example.com"))
                .andExpect(jsonPath("$.short").value(shortCode))
                .andExpect(jsonPath("$.hits").value(42));

        verify(urlService).getLinkByShortCode(shortCode);
    }

    @Test
    @DisplayName("GET /api/v1/urls/{shortCode}/stats - Debe retornar 404 cuando no existe")
    void shouldReturn404WhenStatsNotFound() throws Exception {
        // Given
        String shortCode = "non-existent";

        when(urlService.getLinkByShortCode(shortCode))
                .thenThrow(new ShortCodeNotFoundException("Short code not found: " + shortCode));

        // When & Then
        mockMvc.perform(get("/api/v1/urls/" + shortCode + "/stats"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/urls/short - Debe manejar URLs muy largas")
    void shouldHandleVeryLongUrls() throws Exception {
        // Given
        String longUrl = "https://www.example.com/" + "a".repeat(2000);
        ShortenRequest request = new ShortenRequest(longUrl);
        String shortCode = "compact-code";

        LinkDto linkDto = LinkDto.builder()
                .longUrl(longUrl)
                .shortCode(shortCode)
                .hits(0)
                .build();

        when(urlService.createShortUrl(longUrl)).thenReturn(shortCode);
        when(urlService.getLinkByShortCode(shortCode)).thenReturn(linkDto);

        // When & Then
        mockMvc.perform(post("/api/v1/urls/short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.link.short").value(shortCode));
    }

    @Test
    @DisplayName("Debe manejar caracteres especiales en shortCode")
    void shouldHandleSpecialCharactersInShortCode() throws Exception {
        // Given
        String shortCode = "happy-blue-tiger";
        String longUrl = "https://www.example.com?param=value&other=123";

        when(urlService.getLongUrlAndIncrementHits(shortCode)).thenReturn(longUrl);

        // When & Then
        mockMvc.perform(get("/api/v1/urls/" + shortCode))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", longUrl));
    }
}
