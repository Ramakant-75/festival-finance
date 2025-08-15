package com.example.societyfest.controller;

import com.example.societyfest.dto.*;
import com.example.societyfest.entity.User;
import com.example.societyfest.enums.Role;
import com.example.societyfest.repository.UserRepository;
import com.example.societyfest.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authMgr;
    private final PasswordEncoder encoder;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpDto req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();
        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto req) {
        Authentication auth = authMgr.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        UserDetails ud = (UserDetails) auth.getPrincipal();

        // Fetch actual User entity
        User user = userRepo.findByUsername(ud.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check activation status
        if (!"Y".equalsIgnoreCase(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Your account is not yet activated. Please contact admin."));
        }

        String token = jwtService.generateToken(ud);
        String role = ud.getAuthorities().stream()
                .findFirst()
                .map(granted -> granted.getAuthority())
                .orElse("USER");

        return ResponseEntity.ok(new AuthResponse(token, ud.getUsername(), role));
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userRepo.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(new UserInfoDto(user.getUsername(), user.getRole().name()));
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userRepo.findByUsername(username).isPresent();
        return ResponseEntity.ok(!exists); // true means available
    }


}
