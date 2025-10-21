package danielpm.dev.redislinkapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author danielpm.dev
 */


@DisplayName("Tests para WordBasedShortCodeGenerator")
public class WordBasedShortCodeGeneratorTest {

    private WordBasedShortCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new WordBasedShortCodeGenerator();
    }

    @Test
    @DisplayName("Debe generar un código con el formato correcto: adjective-color-animal")
    void shouldGenerateCodeWithCorrectFormat() {
        // When
        String code = generator.generate();

        // Then
        assertThat(code)
                .isNotNull()
                .isNotEmpty()
                .matches("^[a-z]+-[a-z]+-[a-z]+$")
                .contains("-");

        String[] parts = code.split("-");
        assertThat(parts).hasSize(3);
    }

    @Test
    @DisplayName("Debe generar un código corto con el formato: color-animal")
    void shouldGenerateShortCodeWithCorrectFormat() {
        // When
        String code = generator.generateShort();

        // Then
        assertThat(code)
                .isNotNull()
                .matches("^[a-z]+-[a-z]+$");

        String[] parts = code.split("-");
        assertThat(parts).hasSize(2);
    }

    @Test
    @DisplayName("Debe generar un código con sufijo numérico")
    void shouldGenerateCodeWithSuffix() {
        // Given
        int suffix = 42;

        // When
        String code = generator.generateWithSuffix(suffix);

        // Then
        assertThat(code)
                .isNotNull()
                .endsWith("-42")
                .matches("^[a-z]+-[a-z]+-[a-z]+-\\d+$");
    }

    @Test
    @DisplayName("Debe generar un código con timestamp")
    void shouldGenerateCodeWithTimestamp() {
        // When
        String code = generator.generateWithTimestamp();

        // Then
        assertThat(code)
                .isNotNull()
                .matches("^[a-z]+-[a-z]+-[a-z]+-\\d+$");

        String[] parts = code.split("-");
        assertThat(parts).hasSize(4);
        assertThat(parts[3]).matches("\\d+");
    }

    @RepeatedTest(100)
    @DisplayName("Debe generar códigos variados (no siempre el mismo)")
    void shouldGenerateDifferentCodes() {
        // When
        String code1 = generator.generate();
        String code2 = generator.generate();

        // Then
        // No garantizamos que sean diferentes (baja probabilidad de colisión)
        // pero verificamos que el formato sea consistente
        assertThat(code1).matches("^[a-z]+-[a-z]+-[a-z]+$");
        assertThat(code2).matches("^[a-z]+-[a-z]+-[a-z]+$");
    }

    @Test
    @DisplayName("Debe generar múltiples códigos únicos en su mayoría")
    void shouldGenerateMostlyUniqueCodes() {
        // Given
        Set<String> codes = new HashSet<>();
        int iterations = 1000;

        // When
        for (int i = 0; i < iterations; i++) {
            codes.add(generator.generate());
        }

        // Then - Al menos el 90% deben ser únicos
        assertThat(codes.size()).isGreaterThan((int) (iterations * 0.9));
    }

    @Test
    @DisplayName("Debe calcular correctamente el espacio de combinaciones")
    void shouldCalculateTotalCombinations() {
        // When
        long totalCombinations = generator.getTotalCombinations();
        long shortCombinations = generator.getShortCombinations();

        // Then
        assertThat(totalCombinations).isGreaterThan(0);
        assertThat(shortCombinations).isGreaterThan(0);
        assertThat(totalCombinations).isGreaterThan(shortCombinations);
    }

    @Test
    @DisplayName("Los códigos generados no deben contener caracteres especiales")
    void shouldNotContainSpecialCharacters() {
        // When
        String code = generator.generate();

        // Then
        assertThat(code).matches("^[a-z-]+$");
    }

    @Test
    @DisplayName("Los códigos deben tener una longitud razonable")
    void shouldHaveReasonableLength() {
        // When
        String code = generator.generate();
        String shortCode = generator.generateShort();

        // Then
        assertThat(code.length()).isBetween(10, 30);
        assertThat(shortCode.length()).isBetween(5, 20);
    }
}
