package message.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for the application.
 * This class handles exceptions thrown in any {@link RestController}
 * and maps them to appropriate HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link EntityNotFoundException} thrown by the application.
     * Returns a {@link ProblemDetail} with a NOT_FOUND (404) status and the exception message as the detail.
     *
     * @param ex the exception to be handled
     * @return a {@link ProblemDetail} with status 404 and the exception's message
     */
    @ExceptionHandler(EntityNotFoundException.class)
    private ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
