package com.project.userManagement.exceptions;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.project.userManagement.domain.HttpResponse;
import jakarta.mail.AuthenticationFailedException;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.security.auth.login.AccountLockedException;
import java.io.IOException;
import java.rmi.ServerException;
import java.util.Objects;

import static org.springframework.http.HttpStatus.*;

public class ExceptionHandling implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandling.class);
    private static final String ERROR_PATH = "/error";

    private static final String ACCOUNT_LOCKED_MSG = "Your account has been locked. Please contact administration";
    private static final String METHOD_NOT_ALLOWED_MSG = "This request method is not allowed on this endpoint. Please send a '%s' request";
    private static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
    private static final String INCORRECT_CREDENTIALS_MSG = "Username/password incorrect. Please try again";
    private static final String ACCOUNT_DISABLED_MSG = "Your account has been disabled. Please contact administration";
    private static final String ERROR_PROCESSING_FILE_MSG = "Error occurred while processing file";
    private static final String NOT_ENOUGH_PERMISSION_MSG = "You do not have enough permission";
    private static final String NO_MAPPING_URL_MSG = "There is no mapping for this URL";

    // Generalized method for common exception response
    private ResponseEntity<HttpResponse> createExceptionResponse(HttpStatus status, String message) {
        String formattedMessage = message.toUpperCase();
        return new ResponseEntity<>(new HttpResponse(status.value(), status, status.getReasonPhrase().toUpperCase(), formattedMessage), status);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException() {
        return createExceptionResponse(BAD_REQUEST, ACCOUNT_DISABLED_MSG);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException() {
        return createExceptionResponse(BAD_REQUEST, INCORRECT_CREDENTIALS_MSG);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException() {
        return createExceptionResponse(FORBIDDEN, NOT_ENOUGH_PERMISSION_MSG);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<HttpResponse> lockedException() {
        return createExceptionResponse(UNAUTHORIZED, ACCOUNT_LOCKED_MSG);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception) {
        return createExceptionResponse(UNAUTHORIZED, exception.getMessage());
    }

    // Grouped user-related exceptions in one handler
    @ExceptionHandler({EmailExistException.class, UsernameExistException.class, EmailNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<HttpResponse> handleUserExceptions(Exception exception) {
        return createExceptionResponse(BAD_REQUEST, exception.getMessage());
    }

    // Grouped email-related exceptions
    @ExceptionHandler({AuthenticationFailedException.class, MailSendException.class})
    public ResponseEntity<HttpResponse> emailException() {
        return createExceptionResponse(INTERNAL_SERVER_ERROR, "Email was not sent");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
        return createExceptionResponse(METHOD_NOT_ALLOWED, String.format(METHOD_NOT_ALLOWED_MSG, supportedMethod));
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(ServerException exception) {
        LOGGER.error(exception.getMessage());
        return createExceptionResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException exception) {
        LOGGER.error(exception.getMessage());
        return createExceptionResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> iOException(IOException exception) {
        LOGGER.error(exception.getMessage());
        return createExceptionResponse(INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE_MSG);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<HttpResponse> noHandlerFoundException() {
        return createExceptionResponse(BAD_REQUEST, NO_MAPPING_URL_MSG);
    }

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<HttpResponse> notFound404() {
        return createExceptionResponse(NOT_FOUND, NO_MAPPING_URL_MSG);
    }
}
