package com.orderinventory.auth.service;

import com.orderinventory.auth.dto.AuthResponse;
import com.orderinventory.auth.dto.LoginRequest;
import com.orderinventory.auth.dto.RegisterRequest;
import com.orderinventory.auth.model.User;
import com.orderinventory.auth.repository.UserRepository;
import com.orderinventory.common.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        Set<String> roles = new HashSet<>();
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            roles.add("ROLE_ADMIN");
        } else {
            roles.add("ROLE_CUSTOMER");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                roles
        );
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        List<String> roles = user.getRoles().stream().toList();
        String token = jwtUtil.generateToken(user.getUsername(), roles);

        return new AuthResponse(token, user.getUsername(), roles);
    }
}
