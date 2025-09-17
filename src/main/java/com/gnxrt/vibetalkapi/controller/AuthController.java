package com.gnxrt.vibetalkapi.controller;

import com.gnxrt.vibetalkapi.config.TokenProvider;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.repository.UserRepository;
import com.gnxrt.vibetalkapi.dto.request.LoginRequest;
import com.gnxrt.vibetalkapi.dto.response.ApiResponse;
import com.gnxrt.vibetalkapi.dto.response.AuthResponse;
import com.gnxrt.vibetalkapi.service.CustomUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private TokenProvider tokenProvider;
    private CustomUserService customUserService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          TokenProvider tokenProvider, CustomUserService customUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.customUserService = customUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> createUserHandler(
            @Valid @RequestBody User user,
            HttpServletRequest request) throws UserException {

        String email = user.getEmail();
        String username = user.getUsername();
        String password = user.getPassword();

        if (userRepository.findByEmail(email) != null) {
            throw new UserException("Email already exists: " + email);
        }

        if (userRepository.findByUsername(username) != null) {
            throw new UserException("Username already exists: " + username);
        }

        User createUser = new User();
        createUser.setEmail(email);
        createUser.setUsername(username);
        createUser.setFullName(user.getFullName());
        createUser.setPassword(passwordEncoder.encode(password));

        User savedUser = userRepository.save(createUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse(jwt, true);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandler(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Authentication authentication = getAuthentication(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse(jwt, true);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutHandler() {
        SecurityContextHolder.clearContext();

        ApiResponse response = new ApiResponse("Logged out successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshTokenHandler(
            @RequestHeader("Authorization") String token) throws UserException {

        try {
            String jwt = token.substring(7);
            String email = tokenProvider.getEmailFromToken(jwt);

            if (email == null) {
                throw new UserException("Invalid token");
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UserException("User not found");
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(email, null);
            String newJwt = tokenProvider.generateToken(authentication);

            AuthResponse response = new AuthResponse(newJwt, true);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            throw new UserException("Failed to refresh token: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        Map<String, Object> status = new HashMap<>();

        try {
            if (isAlreadyAuthenticated(request)) {
                String authHeader = request.getHeader("Authorization");
                String jwt = authHeader.substring(7);
                String email = tokenProvider.getEmailFromToken(jwt);
                User user = userRepository.findByEmail(email);

                status.put("authenticated", true);
                status.put("user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName() != null ? user.getFullName() : ""
                ));
            } else {
                status.put("authenticated", false);
                status.put("user", null);
            }
        } catch (Exception e) {
            status.put("authenticated", false);
            status.put("user", null);
            status.put("error", "Invalid token");
        }

        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse> validateToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String email = tokenProvider.getEmailFromToken(jwt);

            if (email != null && userRepository.findByEmail(email) != null) {
                return new ResponseEntity<>(new ApiResponse("Token is valid", true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ApiResponse("Token is invalid", false), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse("Token is invalid", false), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isAlreadyAuthenticated(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String jwt = authHeader.substring(7);
                String email = tokenProvider.getEmailFromToken(jwt);
                return email != null && userRepository.findByEmail(email) != null;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public Authentication getAuthentication(String username, String password) {
        UserDetails userDetail = customUserService.loadUserByUsername(username);
        if (userDetail == null) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!passwordEncoder.matches(password, userDetail.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(userDetail, password, userDetail.getAuthorities());
    }
}