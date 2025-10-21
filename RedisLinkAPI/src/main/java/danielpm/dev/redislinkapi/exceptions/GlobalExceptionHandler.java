package danielpm.dev.redislinkapi.exceptions;

import danielpm.dev.redislinkapi.model.dto.ShortenUrlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * @author danielpm.dev
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortCodeNotFoundException.class)
    public ResponseEntity<ShortenUrlResponse> handleShortCodeNotFound(
            ShortCodeNotFoundException ex) {

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .successful(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ResponseEntity<ShortenUrlResponse> handleShortCodeGeneration(
            ShortCodeGenerationException ex) {

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .successful(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ShortenUrlResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .successful(false)
                .message(errorMessage)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ShortenUrlResponse> handleGenericException(Exception ex) {

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .successful(false)
                .message("Ocurrió un error inesperado: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
