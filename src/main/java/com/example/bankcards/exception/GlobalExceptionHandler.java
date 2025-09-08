package com.example.bankcards.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    public record FieldErrorItem(String field, String message, String code) {
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        log.warn("NotFound: {}", ex.getMessage());
        String code = (ex.getMessage() != null && ex.getMessage().matches("[A-Za-z0-9_.-]+"))
                ? ex.getMessage()
                : "error.not_found";
        String msg = msg(code);
        return build(HttpStatus.NOT_FOUND, code, msg, List.of(), req);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Object> handleDomainValidation(DomainValidationException ex, HttpServletRequest req) {
        log.warn("Validation: {} (field={})", ex.getCode(), ex.getField());
        String msg = msg(ex.getCode(), ex.getArgs());
        List<FieldErrorItem> list = new ArrayList<>();
        if (ex.getField() != null) {
            list.add(new FieldErrorItem(ex.getField(), msg, ex.getCode()));
        }
        return build(ex.getStatus(), ex.getCode(), msg, list, req);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldErrorItem> list = new ArrayList<>();
        for (org.springframework.validation.FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String m = resolveFieldMessage(fe.getDefaultMessage());
            list.add(new FieldErrorItem(fe.getField(), m, fe.getCode()));
        }
        String msg = msg("error.validation");
        return build(HttpStatus.BAD_REQUEST, "error.validation", msg, list, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<FieldErrorItem> list = new ArrayList<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String field = v.getPropertyPath() == null ? null : v.getPropertyPath().toString();
            String m = resolveFieldMessage(v.getMessage());
            String code = v.getConstraintDescriptor() == null || v.getConstraintDescriptor().getAnnotation() == null
                    ? null
                    : v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            list.add(new FieldErrorItem(field, m, code));
        }
        String msg = msg("error.validation");
        return build(HttpStatus.BAD_REQUEST, "error.validation", msg, list, req);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBind(BindException ex, HttpServletRequest req) {
        List<FieldErrorItem> list = new ArrayList<>();
        for (org.springframework.validation.FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String m = resolveFieldMessage(fe.getDefaultMessage());
            list.add(new FieldErrorItem(fe.getField(), m, fe.getCode()));
        }
        String msg = msg("error.validation");
        return build(HttpStatus.BAD_REQUEST, "error.validation", msg, list, req);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("BadJson: {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "error.bad_request", msg("error.bad_request"), List.of(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        log.warn("TypeMismatch: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "error.bad_request", msg("error.bad_request"), List.of(), req);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("MissingParam: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "error.bad_request", msg("error.bad_request"), List.of(), null);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        log.warn("MissingHeader: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "error.bad_request", msg("error.bad_request"), List.of(), req);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("AccessDenied: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "error.access_denied", msg("error.access_denied"), List.of(), req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "error.unauthorized", msg("error.unauthorized"), List.of(), req);
    }


    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("NoHandler: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return build(HttpStatus.NOT_FOUND, "error.not_found", msg("error.not_found"), List.of(), null);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("MethodNotAllowed: {}", ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "error.method_not_allowed", msg("error.method_not_allowed"), List.of(), null);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("UnsupportedMediaType: {}", ex.getMessage());
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "error.unsupported_media_type", msg("error.unsupported_media_type"), List.of(), null);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unhandled: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal", msg("error.internal"), List.of(), req);
    }


    private ResponseEntity<Object> build(HttpStatus status, String code, String message, List<FieldErrorItem> errors, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setTitle(status.getReasonPhrase());
        if (req != null) {
            try {
                pd.setInstance(URI.create(req.getRequestURI()));
            } catch (Exception ignored) {
            }
        }
        Instant now = Instant.now();
        pd.setProperty("timestamp", DateTimeFormatter.ISO_INSTANT.format(now));
        pd.setProperty("epochMillis", now.toEpochMilli());
        if (code != null && !code.isBlank()) {
            pd.setProperty("code", code);
            try {
                pd.setType(URI.create("urn:error:%s".formatted(code)));
            } catch (Exception ignored) {
            }
        }
        String traceId = MDC.get("traceId");
        if (traceId != null) pd.setProperty("traceId", traceId);
        String path = MDC.get("path");
        if (path != null) pd.setProperty("path", path);
        if (errors != null && !errors.isEmpty()) {
            pd.setProperty("errors", errors);
        }
        return ResponseEntity.status(status).body(pd);
    }

    private String msg(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messages.getMessage(code, args, code, locale);
    }

    private String resolveFieldMessage(String defaultMessage) {
        if (defaultMessage == null) return null;
        if (defaultMessage.matches("[a-zA-Z0-9_.-]+")) {
            return msg(defaultMessage);
        }
        return defaultMessage;
    }
}