package com.example.societyfest.exception;

import com.example.societyfest.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Slf4j
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
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        String sarcasticMessage = SARCASTIC_MESSAGES.get(random.nextInt(SARCASTIC_MESSAGES.size()));
        
        log.warn("Bad credentials attempt. Exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .mainMessage("Username or password incorrect")  // This will be highlighted on UI
                .additionalMessage(sarcasticMessage)            // Optional sarcastic message
                .errorCode("INVALID_CREDENTIALS")
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthExceptions(AuthenticationException ex) {
        log.warn("Authentication exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .mainMessage("Authentication failed")
                .additionalMessage("Please check your credentials.")
                .errorCode("AUTH_FAILED")
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }
}

