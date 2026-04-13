package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService service;

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long id) {
        return service.getUserById(id);
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto user) {
        return service.addUser(user);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long id, @Valid @RequestBody UserDto newUserDto) {
        return service.updateUser(id, newUserDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long id) {
        service.deleteUserById(id);
    }
}