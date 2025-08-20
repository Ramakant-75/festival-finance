package com.example.societyfest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Wrong credentials. But don’t worry, we’ve all forgotten worse things… like exes’ birthdays \uD83D\uDC40"));
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<Map<String, String>> handleAuthExceptions(AuthenticationException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication failed. Please check your credentials."));
        }


    }


