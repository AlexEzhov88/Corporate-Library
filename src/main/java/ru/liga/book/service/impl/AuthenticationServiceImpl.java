package ru.liga.book.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.liga.book.config.JwtService;
import ru.liga.book.dto.JwtResponse;
import ru.liga.book.dto.LoginRequest;
import ru.liga.book.dto.RegisterRequest;
import ru.liga.book.dto.UserResponse;
import ru.liga.book.exception.UserAlreadyExistsException;
import ru.liga.book.model.Role;
import ru.liga.book.model.Token;
import ru.liga.book.model.User;
import ru.liga.book.repository.RoleRepository;
import ru.liga.book.repository.TokenRepository;
import ru.liga.book.repository.UserRepository;
import ru.liga.book.service.AuthenticationService;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public UserResponse registerNewUser(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User with username already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);

        return new UserResponse(user.getId(), user.getUsername());
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);

        // Сохраняем токен в базу данных
        Token token = new Token();
        token.setToken(jwt);
        token.setUser((User) userDetails);
        token.setExpired(false);
        token.setRevoked(false);
        tokenRepository.save(token);

        return new JwtResponse(jwt);
    }

}