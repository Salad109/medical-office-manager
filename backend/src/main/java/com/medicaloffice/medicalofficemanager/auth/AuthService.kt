package com.medicaloffice.medicalofficemanager.auth;

import com.medicaloffice.medicalofficemanager.auth.dto.AuthResponse;
import com.medicaloffice.medicalofficemanager.auth.dto.LoginRequest;
import com.medicaloffice.medicalofficemanager.users.Role;
import com.medicaloffice.medicalofficemanager.users.User;
import com.medicaloffice.medicalofficemanager.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        String role = "";
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            role = customUserDetails.getRole();
        }

        log.info("User '{}' logged in successfully", request.username());
        return new AuthResponse(token, role);
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        if (request.role() == Role.PATIENT &&
                (request.pesel() == null || request.pesel().trim().isEmpty())) {
            throw new IllegalArgumentException("PESEL is required for patients");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setPesel(request.pesel());
        user.setRole(request.role());

        userRepository.save(user);

        log.info("User '{}' registered successfully with role {}", request.username(), request.role());
        return new RegisterResponse("User registered successfully", request.username());
    }
}
