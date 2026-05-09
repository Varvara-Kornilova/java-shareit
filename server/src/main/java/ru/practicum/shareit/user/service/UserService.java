package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto addUser(UserDto userDto);

    UserUpdateDto updateUser(Long id, UserUpdateDto newUserDto);

    void deleteUserById(Long id);
}