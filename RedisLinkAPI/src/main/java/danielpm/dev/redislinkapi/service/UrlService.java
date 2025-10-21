package danielpm.dev.redislinkapi.service;

import danielpm.dev.redislinkapi.model.dto.LinkDto;

/**
 * @author danielpm.dev
 */
public interface UrlService {

    String createShortUrl(String longUrl);
    LinkDto getLinkByShortCode(String shortCode);
    String getLongUrlAndIncrementHits(String shortCode);
    String createShortUrlWithTTL(String longUrl, long seconds);
}
