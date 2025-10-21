package danielpm.dev.redislinkapi.model.dto;

import lombok.Builder;

/**
 * @author danielpm.dev
 */
@Builder
public record ShortenUrlResponse(
        Boolean successful,
        String message,
        LinkDto link,
        String shortUrl
) {}

