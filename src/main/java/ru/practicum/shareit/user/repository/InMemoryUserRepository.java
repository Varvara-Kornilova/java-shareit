package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final List<User> users = new ArrayList<>();
    private long nextId = 1;

    @Override
    public Collection<User> findAll() {
        return List.copyOf(users);
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return users.stream()
                .filter(user -> user.getId() != null
                                   && user.getId().equals(userId))
                .findFirst();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        } else {
            users.removeIf(existing -> existing.getId().equals(user.getId()));
        }

        users.add(user);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail() != null && user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public void delete(Long id) {
        users.removeIf(user -> user.getId() != null && user.getId().equals(id));
    }
}