package com.fairshare.distributed_expense_splitter.exception;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;
import com.fairshare.distributed_expense_splitter.entity.ErrorResponse;
import com.fairshare.distributed_expense_splitter.entity.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

  @Autowired
  private Environment environment;

  private static final Logger LOGGER = LogManager.getLogger(
    GlobalExceptionHandler.class
  );

  @ExceptionHandler({UserException.class, GroupException.class})
  public ResponseEntity<ErrorResponse> handleNotFound(
    UserException ex,
    HttpServletRequest request
  ) {
    ErrorResponse response = ErrorResponse
      .builder()
      .timestamp(OffsetDateTime.now())
      .status(HttpStatus.NOT_FOUND.value())
      .error(HttpStatus.NOT_FOUND.getReasonPhrase())
      .code(ex.getErrorCode().name())
      .message(ex.getMessage())
      .path(request.getRequestURI())
      .build();

      LOGGER.error(environment.getProperty("error.not_found", "Resource not found: {}"), ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(
    Exception ex,
    HttpServletRequest request
  ) {
    ErrorResponse response = ErrorResponse
      .builder()
      .timestamp(OffsetDateTime.now())
      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
      .code(ErrorCode.INTERNAL_SERVER_ERROR.name())
      .message("Unexpected error occurred " + ex.getMessage())
      .path(request.getRequestURI())
      .build();
      LOGGER.error(environment.getProperty("error.internal", "Internal server error: {}"), ex.getMessage());
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex,
    HttpServletRequest request
  ) {
    List<FieldError> fieldErrors = ex
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .map(fe ->
        new FieldError(
          fe.getField(),
          fe.getRejectedValue(),
          fe.getDefaultMessage()
        )
      )
      .collect(Collectors.toList());

    ErrorResponse response = ErrorResponse
      .builder()
      .timestamp(OffsetDateTime.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
      .code(ErrorCode.VALIDATION_ERROR.name())
      .message("Method argument validation failed")
      .path(request.getRequestURI())
      .fieldErrors(fieldErrors)
      .build();

      LOGGER.error(environment.getProperty("error.validation", "Validation error: {}"), fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
    ConstraintViolationException ex,
    HttpServletRequest request
  ) {
    List<FieldError> fieldErrors = ex
      .getConstraintViolations()
      .stream()
      .map(cv -> {
        String path = "";
        try {
          Path propertyPath = cv.getPropertyPath();
          path = propertyPath.toString();
        } catch (Exception e) {
          path = cv.getPropertyPath().toString();
        }
        return new FieldError(path, cv.getInvalidValue(), cv.getMessage());
      })
      .collect(Collectors.toList());

    ErrorResponse response = ErrorResponse
      .builder()
      .timestamp(OffsetDateTime.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
      .code(ErrorCode.VALIDATION_ERROR.name())
      .message("Request validation failed")
      .path(request.getRequestURI())
      .fieldErrors(fieldErrors)
      .build();

      LOGGER.error(environment.getProperty("error.validation", "Validation error: {}"), fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}
