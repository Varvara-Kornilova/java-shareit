package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDto_MapsAllFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@test.com");

        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("Иван", dto.getName());
        assertEquals("ivan@test.com", dto.getEmail());
    }

    @Test
    void toUser_FromUserDto() {
        UserDto dto = new UserDto(null, "Иван", "ivan@test.com");

        User user = UserMapper.toUser(dto);

        assertEquals("Иван", user.getName());
        assertEquals("ivan@test.com", user.getEmail());
        assertNull(user.getId());
    }

    @Test
    void toUserUpdateDto_MapsAllFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@test.com");

        UserUpdateDto updateDto = UserMapper.toUserUpdateDto(user);

        assertEquals(1L, updateDto.getId());
        assertEquals("Иван", updateDto.getName());
        assertEquals("ivan@test.com", updateDto.getEmail());
    }

    @Test
    void toUser_FromUserUpdateDto() {
        UserUpdateDto updateDto = new UserUpdateDto(1L, "Иван", "ivan@test.com");

        User user = UserMapper.toUser(updateDto);

        assertEquals("Иван", user.getName());
        assertEquals("ivan@test.com", user.getEmail());
    }
}