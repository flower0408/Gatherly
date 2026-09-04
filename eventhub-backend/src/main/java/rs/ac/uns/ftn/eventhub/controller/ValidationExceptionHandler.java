package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// Kada validacija DTO-a ne prodje, klijentu se vraca 400 sa spiskom polja i porukama,
// umesto podrazumevanog Spring odgovora koji je preopsiran za prikaz u formi.
@RestControllerAdvice
public class ValidationExceptionHandler {

    private static final Logger logger = LogManager.getLogger(ValidationExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        logger.error("Validation failed for fields: " + errors.keySet());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
