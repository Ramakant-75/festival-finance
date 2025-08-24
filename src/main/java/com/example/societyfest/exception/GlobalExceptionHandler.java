package com.example.societyfest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final List<String> SARCASTIC_MESSAGES = List.of(
            "Are you a Rohit Sharma fan 👀, because you keep forgetting your credentials 🤣",
            "Wrong credentials again? Maybe try remembering this time 😜",
            "Credentials missing? Don't worry, even Einstein forgot once 🤭",
            "Oops! That's not right. Maybe the keyboard hates you today 🤪",
            "Wrong credentials. Did you leave your brain at home? 😂",
            "Incorrect again! Are you testing my patience? 😎",
            "Login fail! Maybe try entering your ex birthday? 🤣",
            "Incorrect credentials. Maybe your memory went on vacation? 🌴",
            "Access denied. Perhaps you should try with your eyes open 👀",
            "Credentials incorrect! Did you borrow it from a fortune cookie? 🥠",
            "Login failed! I know one should be creative but not this time 😭"
    );

    private final Random random = new Random();

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
            String message = SARCASTIC_MESSAGES.get(random.nextInt(SARCASTIC_MESSAGES.size()));
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", message));
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<Map<String, String>> handleAuthExceptions(AuthenticationException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication failed. Please check your credentials."));
        }


    }


