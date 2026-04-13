package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    UserDto addUser(UserDto user);
    UserDto updateUser(Long id, UserDto newUserDto);
    void deleteUserById(Long id);
}