package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Collection<User> findAll();
    Optional<User> findUserById(Long userId);
    User save(User user);
    Optional<User> findByEmail(String email);
    void delete(Long userId);
}