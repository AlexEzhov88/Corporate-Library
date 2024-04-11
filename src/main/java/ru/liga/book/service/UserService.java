package ru.liga.book.service;

import ru.liga.book.model.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);

    void deleteUser(Long userId);

    User updateUser(User user);
}