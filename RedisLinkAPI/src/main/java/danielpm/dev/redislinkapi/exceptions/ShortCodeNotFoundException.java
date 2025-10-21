package danielpm.dev.redislinkapi.exceptions;

/**
 * @author danielpm.dev
 */
public class ShortCodeNotFoundException extends RuntimeException {
    public ShortCodeNotFoundException(String message) {
        super(message);
    }
}
