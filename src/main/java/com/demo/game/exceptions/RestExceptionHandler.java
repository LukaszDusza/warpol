package com.demo.game.exceptions;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Globalny <b>ControllerAdvice</b> mapujący wyjątki domenowe na kody HTTP.
 * <p>
 * • {@link IllegalArgumentException}        → 400  BadRequest<br> • {@link org.springframework.data.crossstore.ChangeSetPersister.NotFoundException} → 404  NotFound<br> • Walidacja beanów (JSR‑380)
 * → 422  UnprocessableEntity
 */
@Slf4j
@RestControllerAdvice
@Hidden
public class RestExceptionHandler {

  /* -------------------------------------------------------------- */
  /* 400 Bad Request – nielegalne komendy itp.                      */
  /* -------------------------------------------------------------- */

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemJson> handleBadRequest(IllegalArgumentException ex) {
    log.debug("400 Bad Request: {}", ex.getMessage());
    return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  /* -------------------------------------------------------------- */
  /* 404 Not Found – brak encji                                     */
  /* -------------------------------------------------------------- */

  @ExceptionHandler(org.springframework.data.crossstore.ChangeSetPersister.NotFoundException.class)
  public ResponseEntity<ProblemJson> handleNotFound(Exception ex) {
    log.debug("404 Not Found: {}", ex.getMessage());
    return problem(HttpStatus.NOT_FOUND, "Resource not found");
  }

  /* -------------------------------------------------------------- */
  /* 422 Validation errors (Bean Validation, @Valid)                */
  /* -------------------------------------------------------------- */

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ProblemJson> handleValidation(Exception ex) {
    Map<String, String> details = new HashMap<>();
    if (ex instanceof MethodArgumentNotValidException manv) {
      manv.getBindingResult().getFieldErrors().forEach(err ->
          details.put(err.getField(), err.getDefaultMessage()));
    } else if (ex instanceof BindException be) {
      be.getBindingResult().getFieldErrors().forEach(err ->
          details.put(err.getField(), err.getDefaultMessage()));
    }
    log.debug("422 Unprocessable Entity: {}", details);
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed", details);
  }

  /* -------------------------------------------------------------- */
  /* 500 Internal Server Error – nieoczekiwane awarie               */
  /* -------------------------------------------------------------- */

  /**
   * „Bezpieczna siatka” dla wszystkich nieobsłużonych wyjątków, w tym NullPointerException.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemJson> handleGeneric(Exception ex) {
    log.error("500 Internal Server Error", ex);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR,
        "Unexpected server error");
  }

  /* -------------------------------------------------------------- */
  /* Helper                                                          */
  /* -------------------------------------------------------------- */

  private ResponseEntity<ProblemJson> problem(HttpStatus status, String msg) {
    return problem(status, msg, null);
  }

  private ResponseEntity<ProblemJson> problem(HttpStatus status, String msg, Map<String, String> extra) {
    ProblemJson body = new ProblemJson(status.value(), msg, extra);
    return ResponseEntity.status(status).body(body);
  }

  /* -------------------------------------------------------------- */
  /* DTO                                                             */
  /* -------------------------------------------------------------- */

  public record ProblemJson(
      int status,
      String message,
      Map<String, String> errors,
      @JsonFormat(shape = JsonFormat.Shape.STRING)
      Instant timestamp
  ) {

    public ProblemJson(int status, String message, Map<String, String> errors) {
      this(status, message, errors, Instant.now());
    }
  }
}
