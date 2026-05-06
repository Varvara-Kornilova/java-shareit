package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.debug("Отправляем запрос на получение пользователя по ID {}", userId);
        User user = findUserByIdOrThrow(userId);
        log.debug("Пользователь с id #{} найден", userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto addUser(UserDto userDto) throws DuplicatedDataException {
        log.debug("Запрос на добавление нового пользователя: имя = {}, email = {}",
                userDto.getName(), userDto.getEmail());

        validateEmailIsUnique(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        repository.save(user);
        log.debug("Новый пользователь с id #{} успешно добавлен", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserUpdateDto updateUser(Long id, UserUpdateDto newUserDto) {
        log.debug("Отправляем запрос на обновление пользователя с ID {}", id);

        User existingUser = findUserByIdOrThrow(id);

        if (newUserDto.getEmail() != null && !newUserDto.getEmail().equals(existingUser.getEmail())) {
            validateEmailIsUnique(newUserDto.getEmail(), id);
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
    @Transactional
    public void deleteUserById(Long id) {
        log.debug("Удаление пользователя с id={}", id);

        findUserByIdOrThrow(id);

        repository.deleteById(id);
        log.info("Пользователь с id={} успешно удалён", id);
    }

    private User findUserByIdOrThrow(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id #{} не найден", userId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });
    }

    private void validateEmailIsUnique(String email, Long excludeUserId) {
        Optional<User> existingUser = repository.findByEmail(email);

        if (existingUser.isPresent()) {
            if (excludeUserId != null && existingUser.get().getId().equals(excludeUserId)) {
                return;
            }

            log.warn("email {} уже используется, операция невозможна", email);
            throw new DuplicatedDataException("Данный email уже используется");
        }
    }
}