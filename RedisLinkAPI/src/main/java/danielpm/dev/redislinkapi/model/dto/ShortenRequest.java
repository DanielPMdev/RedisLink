package danielpm.dev.redislinkapi.model.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * @author danielpm.dev
 */
public record ShortenRequest(
        @NotBlank(message = "La URL no puede estar vacía")
        @URL(message = "La URL debe ser válida")
        String url
) {}
