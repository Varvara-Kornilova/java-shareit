package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addItem_WithRequestId_ShouldReturn200() throws Exception {
        ItemDto newItem = new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), 1L);
        ItemDto created = new ItemDto(10L, "Дрель", "Профессиональная", true, null, null, List.of(), 1L);

        when(itemService.addItem(eq(1L), any(ItemDto.class))).thenReturn(created);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.requestId").value(1));
    }

    @Test
    void addItem_WithoutName_ShouldReturn400() throws Exception {
        ItemDto invalidItem = new ItemDto(null, "", "Описание", true, null, null, List.of(), null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_ShouldReturn200() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto("Отличный товар!");
        CommentDto response = new CommentDto(5L, "Отличный товар!", "User", LocalDateTime.now());

        when(itemService.addComment(eq(1L), eq(10L), any(CommentCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.text").value("Отличный товар!"))
                .andExpect(jsonPath("$.authorName").value("User"));
    }

    @Test
    void getItemById_WithComments_ShouldReturnComments() throws Exception {
        ItemDto item = new ItemDto(10L, "Дрель", "Профессиональная", true, null, null,
                List.of(new CommentDto(1L, "Good", "User", LocalDateTime.now())), null);

        when(itemService.getItemById(10L)).thenReturn(item);

        mockMvc.perform(get("/items/10")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].text").value("Good"));
    }
}