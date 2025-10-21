package danielpm.dev.redislinkapi.util;

/**
 * @author danielpm.dev
 */
public class Base62Encoder {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();

    public static String encode(long number) {
        if (number == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % BASE);
            sb.append(ALPHABET.charAt(remainder));
            number /= BASE;
        }

        // Invertimos el orden porque los dígitos se generan al revés
        return sb.reverse().toString();
    }
}

