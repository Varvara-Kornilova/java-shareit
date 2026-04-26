package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public Collection<UserDto> getAllUsers() {
        log.debug("Запрошен список всех пользователей");
        Collection<UserDto> users = repository.findAll()
                        .stream()
                        .map(UserMapper::toUserDto)
                        .collect(Collectors.toList());
        log.debug("Найдено {} пользователей", users.size());
        return users;
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.debug("Отправляем запрос на получение пользователя по ID {}", userId);
        User user = repository.findUserById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id #{} не найден", userId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });
    log.debug("Пользователь с id #{} найден", userId);
    return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto addUser(UserDto userDto) throws DuplicatedDataException {
        log.debug("Запрос на добавление нового пользователя: имя = {}, email = {}", userDto.getName(), userDto.getEmail());
        Optional<User> alreadyExistsUser = repository.findByEmail(userDto.getEmail());

        if (alreadyExistsUser.isPresent()) {
            log.warn("email {} уже используется, добавление пользователя невозможно", userDto.getEmail());
            throw new DuplicatedDataException("Данный email уже используется");
        }

        User user = UserMapper.toUser(userDto);
        repository.save(user);
        log.debug("Новый пользователь с id #{} успешно добавлен", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserUpdateDto updateUser(Long id, UserUpdateDto newUserDto) {
        log.debug("Отправляем запрос на обновление пользователя с ID {}", id);
        User existingUser = repository.findUserById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id #{} не найден", id);
                    return new NotFoundException("Пользователь не найден");
                });

        if (newUserDto.getEmail() != null && !newUserDto.getEmail().equals(existingUser.getEmail())) {
            Optional<User> emailOwner = repository.findByEmail(newUserDto.getEmail());

            if (emailOwner.isPresent() && emailOwner.get().getEmail().equals(newUserDto.getEmail())) {
                log.warn("email '{}' пользователя уже используется другим пользователем", newUserDto.getEmail());
                throw new DuplicatedDataException("Данный email уже используется");
            }
        }

        if (newUserDto.getName() != null) {
            existingUser.setName(newUserDto.getName());
        }
        if (newUserDto.getEmail() != null) {
            existingUser.setEmail(newUserDto.getEmail());
        }

        User updatedUser = repository.save(existingUser);
        log.debug("Пользователь с id = {} успешно обновлен", id);

        return UserMapper.toUserUpdateDto(updatedUser);
    }

    @Override
    public void deleteUserById(Long id) {

        log.debug("Удаление пользователя с id={}", id);

        if (repository.findUserById(id).isEmpty()) {
            log.warn("Попытка удаления несуществующего пользователя с id={}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        repository.delete(id);
        log.info("Пользователь с id={} успешно удалён", id);
    }
}