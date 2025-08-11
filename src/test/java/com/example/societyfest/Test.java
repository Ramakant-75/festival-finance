package com.example.societyfest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test {


        public static void main(String[] args) {
            String rawPassword = "admin123";  // Your desired password
            String encoded = new BCryptPasswordEncoder().encode(rawPassword);
            System.out.println(encoded);
        }
    }

