package com.harshit.notesapp.service;

import com.harshit.notesapp.dto.AuthResponse;
import com.harshit.notesapp.dto.LoginRequest;
import com.harshit.notesapp.dto.RegisterRequest;
import com.harshit.notesapp.entity.User;
import com.harshit.notesapp.repository.UserRepository;
import com.harshit.notesapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public void register(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        // 2. Create the User entity with hashed password
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // 3. Save to database
        @SuppressWarnings({"null", "unused"})
        User savedUser = userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate user via AuthenticationManager
        // This will hash the provided password and securely compare it against the database.
        // It throws BadCredentialsException automatically if they don't match.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. If authentication passed, fetch the user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // 3. Convert to Spring Security UserDetails format
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()
        );

        // 4. Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);
        
        // 5. Return response
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .build();
    }
}
