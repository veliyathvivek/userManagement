package com.project.userManagement.exceptions;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.project.userManagement.domain.HttpResponse;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class ExceptionHandling {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandling.class);

    // Generalized method for common exception response
    private ResponseEntity<HttpResponse> createExceptionResponse(HttpStatus status, String message) {
        String formattedMessage = message.toUpperCase();
        return new ResponseEntity<>(
                new HttpResponse(
                        status.value(), status, formattedMessage), status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException(AccessDeniedException e) {
        LOGGER.error(e.getMessage());
        return createExceptionResponse(FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException e) {
        LOGGER.error(e.getMessage());
        return createExceptionResponse(UNAUTHORIZED, e.getMessage());
    }

    // Grouped user-related exceptions in one handler
    @ExceptionHandler({
            EmailExistException.class, UsernameExistException.class, EmailNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<HttpResponse> handleUserExceptions(Exception e) {
        LOGGER.error(e.getMessage());
        return createExceptionResponse(BAD_REQUEST, e.getMessage());
    }

    // Grouped email-related exceptions
    @ExceptionHandler( MailSendException.class)
    public ResponseEntity<HttpResponse> emailException(MailSendException e) {
        LOGGER.error(e.getMessage());
        return createExceptionResponse(INTERNAL_SERVER_ERROR, Objects.requireNonNull(e.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        LOGGER.error(e.getMessage());
        HttpMethod supportedMethod = Objects.requireNonNull(e.getSupportedHttpMethods()).iterator().next();
        return createExceptionResponse(METHOD_NOT_ALLOWED, String.format(e.getMessage(), supportedMethod));
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(ServerException e) {
        LOGGER.error(e.getMessage());
        return createExceptionResponse(INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException e) {
        LOGGER.error(e.getMessage());
        return createExceptionResponse(NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> iOException(IOException e) {
        LOGGER.error(e.getMessage());
        return createExceptionResponse(INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<HttpResponse>  handleNoHandlerFoundException(NoHandlerFoundException e) {
        return createExceptionResponse(NOT_FOUND, "No URL is mapped");
    }
}
