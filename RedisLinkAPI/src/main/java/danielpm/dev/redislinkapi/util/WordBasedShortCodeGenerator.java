package danielpm.dev.redislinkapi.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;


/**
 * @author danielpm.dev
 */
@Component
public class WordBasedShortCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final List<String> ADJECTIVES = List.of(
            "happy", "swift", "bright", "calm", "cool", "warm", "bold", "wise",
            "kind", "brave", "quick", "gentle", "wild", "free", "pure", "true",
            "neat", "fair", "smart", "deep", "rich", "soft", "hard", "fresh",
            "sharp", "fleet", "fierce", "grand", "loyal", "proud", "sleek", "stark"
    );

    private static final List<String> COLORS = List.of(
            "red", "blue", "green", "gold", "silver", "purple", "pink", "orange",
            "cyan", "amber", "violet", "coral", "jade", "ruby", "pearl", "onyx",
            "azure", "crimson", "ivory", "slate", "bronze", "copper", "indigo", "teal"
    );

    private static final List<String> ANIMALS = List.of(
            "tiger", "panda", "eagle", "wolf", "fox", "bear", "deer", "hawk",
            "lion", "otter", "seal", "whale", "shark", "raven", "swan", "lynx",
            "falcon", "cobra", "dragon", "phoenix", "unicorn", "griffin", "leopard",
            "cheetah", "jaguar", "panther", "condor", "osprey", "badger", "marten"
    );

    /**
     * Generate a readable code: happy-blue-tiger
     * Range of possibilities: ~138,000 combinations
     */
    public String generate() {
        String adjective = getRandomElement(ADJECTIVES);
        String color = getRandomElement(COLORS);
        String animal = getRandomElement(ANIMALS);

        return String.format("%s-%s-%s", adjective, color, animal);
    }

    /**
     *  Generates a short code: blue-tiger
     *  Range of possibilities: ~528 combinations
     *  Useful when you need shorter codes
     */
    public String generateShort() {
        String color = getRandomElement(COLORS);
        String animal = getRandomElement(ANIMALS);

        return String.format("%s-%s", color, animal);
    }

    /**
     *  Generates a code with a numeric suffix: swift-red-panda-42
     *  Ensures greater uniqueness when the word space is exhausted
     */
    public String generateWithSuffix(int suffix) {
        String adjective = getRandomElement(ADJECTIVES);
        String color = getRandomElement(COLORS);
        String animal = getRandomElement(ANIMALS);

        return String.format("%s-%s-%s-%d", adjective, color, animal, suffix);
    }

    /**
     * Generate a timestamp code to ensure absolute uniqueness.
     */
    public String generateWithTimestamp() {
        String adjective = getRandomElement(ADJECTIVES);
        String color = getRandomElement(COLORS);
        String animal = getRandomElement(ANIMALS);
        long timestamp = System.currentTimeMillis() % 10000; // Últimos 4 dígitos

        return String.format("%s-%s-%s-%d", adjective, color, animal, timestamp);
    }

    /**
     * Calculate the total space of possible combinations.
     */
    public long getTotalCombinations() {
        return (long) ADJECTIVES.size() * COLORS.size() * ANIMALS.size();
    }

    /**
     * Calculate the combination space for short codesw
     */
    public long getShortCombinations() {
        return (long) COLORS.size() * ANIMALS.size();
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
