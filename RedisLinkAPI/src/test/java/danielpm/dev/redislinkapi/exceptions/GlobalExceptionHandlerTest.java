package danielpm.dev.redislinkapi.exceptions;

import danielpm.dev.redislinkapi.controller.UrlController;
import danielpm.dev.redislinkapi.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author danielpm.dev
 */
@WebMvcTest(controllers = {UrlController.class, GlobalExceptionHandler.class})
@DisplayName("Tests para GlobalExceptionHandler")
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    @DisplayName("Debe manejar ShortCodeNotFoundException con 404")
    void shouldHandleShortCodeNotFoundException() throws Exception {
        // Given
        String shortCode = "non-existent";
        when(urlService.getLongUrlAndIncrementHits(shortCode))
                .thenThrow(new ShortCodeNotFoundException("Short code not found: " + shortCode));

        // When & Then
        mockMvc.perform(get("/api/v1/urls/" + shortCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.successful").value(false))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @DisplayName("Debe manejar ShortCodeGenerationException con 500")
    void shouldHandleShortCodeGenerationException() throws Exception {
        // Given
        String request = "{\"url\":\"https://www.example.com\"}";
        when(urlService.createShortUrl(anyString()))
                .thenThrow(new ShortCodeGenerationException("No se pudo generar código único"));

        // When & Then
        mockMvc.perform(post("/api/v1/urls/short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.successful").value(false))
                .andExpect(jsonPath("$.message").value(containsString("No se pudo generar")));
    }

    @Test
    @DisplayName("Debe manejar validaciones con 400")
    void shouldHandleValidationExceptions() throws Exception {
        // Given
        String invalidRequest = "{\"url\":\"\"}";

        // When & Then
        mockMvc.perform(post("/api/v1/urls/short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.successful").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
}
