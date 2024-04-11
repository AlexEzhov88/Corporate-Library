package ru.liga.book.service;

import ru.liga.book.dto.LoginRequest;
import ru.liga.book.dto.RegisterRequest;
import ru.liga.book.dto.JwtResponse;
import ru.liga.book.dto.UserResponse;

public interface AuthenticationService {
    UserResponse registerNewUser(RegisterRequest registerRequest);

    JwtResponse authenticateUser(LoginRequest loginRequest);
}