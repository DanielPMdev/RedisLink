package danielpm.dev.redislinkapi.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * @author danielpm.dev
 */
@Builder
public record LinkDto(

        @JsonProperty("long_url")
        String longUrl,

        @JsonProperty("short")
        String shortCode,

        @JsonProperty("hits")
        Integer hits,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {}
