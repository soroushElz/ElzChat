package com.example.ChatApplication.Handler;

import com.example.ChatApplication.Exception.ActivationTokenException;
import com.example.ChatApplication.Exception.ChangePasswordTokenException;
import com.example.ChatApplication.Exception.OperationNotPermittedException;
import com.example.ChatApplication.Exception.TokenRefreshException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;

import static com.example.ChatApplication.Handler.BusinessErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler  {



    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException() {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode())
                                .businessErrorCodeDescription(BAD_CREDENTIALS.getDescription())
                                .error("Login and / or Password is incorrect")
                                .build()
                );
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleException(OperationNotPermittedException exp) {
        exp.printStackTrace();
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(MissingResourceException.class)
    public ResponseEntity<ExceptionResponse> handleMissingResourceException(Exception exp){
        exp.printStackTrace();
        return ResponseEntity
                .status(CONFLICT)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCodeDescription("Resource Not Available")
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp) {

        exp.printStackTrace();
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCodeDescription("Internal error, please contact the admin")
                                .error(exp.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException  exp){
        return ResponseEntity.status(FORBIDDEN)
                .body(exp.getMessage()
                );

    }


    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Set<String> errors = new HashSet<String>();
        ex.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String errorMessage = error.getDefaultMessage();
                    errors.add(errorMessage);
                });

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .validationErrors(errors)
                                .build()
                );
    }


    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleMessagingException(MessagingException exp) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                );
    }





    @ExceptionHandler(value = TokenRefreshException.class)
    public ResponseEntity<ExceptionResponse> handleRefreshTokenException(TokenRefreshException exp) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(REFRESH_TOKEN_EXPIRED.getCode())
                                .businessErrorCodeDescription(REFRESH_TOKEN_EXPIRED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidJson(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String field = ife.getPath().get(0).getFieldName();
            String allowed = Arrays.toString(ife.getTargetType().getEnumConstants());
            return ResponseEntity.badRequest()
                    .body("Invalid value for field '" + field + "'. Allowed values: " + allowed);
        }
        return ResponseEntity.badRequest().body("Malformed request");
    }
}


