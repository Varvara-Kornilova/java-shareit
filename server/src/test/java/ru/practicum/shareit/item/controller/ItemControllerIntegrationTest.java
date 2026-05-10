package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private final long user_id = 1L;
    private final long item_id = 10L;

    @Test
    void getAllItems_Success() throws Exception {
        when(itemService.getAllItems(eq(user_id)))
                .thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", user_id))
                .andExpect(status().isOk());

        verify(itemService).getAllItems(eq(user_id));
    }

    @Test
    void getItemById_Success() throws Exception {
        ItemDto item = new ItemDto(item_id, "Дрель", "Профессиональная", true, null, null, List.of(), null);
        when(itemService.getItem(eq(item_id))).thenReturn(item);

        mockMvc.perform(get("/items/{itemId}", item_id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item_id))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getItemById_NotFound_ThrowsException() throws Exception {
        when(itemService.getItem(eq(item_id)))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/{itemId}", item_id))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_WithText_Success() throws Exception {
        when(itemService.search(eq("дрель")))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemService).search(eq("дрель"));
    }

    @Test
    void search_WithBlankText_ReturnsEmpty() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void search_WithoutText_ReturnsEmpty() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void addItem_Success() throws Exception {
        ItemDto newItem = new ItemDto(null, "Дрель", "Профессиональная", true, null, null, List.of(), null);
        ItemDto created = new ItemDto(item_id, "Дрель", "Профессиональная", true, null, null, List.of(), null);

        when(itemService.addItem(eq(user_id), any(ItemDto.class)))
                .thenReturn(created);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", user_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(item_id));

        verify(itemService).addItem(eq(user_id), any(ItemDto.class));
    }

    @Test
    void updateItem_Success() throws Exception {
        ItemUpdateDto updateDto = new ItemUpdateDto(item_id, "Новое название", "Новое описание", false);
        ItemDto updated = new ItemDto(item_id, "Новое название", "Новое описание", false, null, null, List.of(), null);

        when(itemService.updateItem(eq(user_id), eq(item_id), any(ItemUpdateDto.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/items/{itemId}", item_id)
                        .header("X-Sharer-User-Id", user_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateItem_AccessDenied_ThrowsException() throws Exception {
        ItemUpdateDto updateDto = new ItemUpdateDto(item_id, "Новое название", "Описание", true);

        when(itemService.updateItem(eq(user_id), eq(item_id), any(ItemUpdateDto.class)))
                .thenThrow(new AccessDeniedException("Только владелец может редактировать"));

        mockMvc.perform(patch("/items/{itemId}", item_id)
                        .header("X-Sharer-User-Id", user_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteItem_Success() throws Exception {
        doNothing().when(itemService).deleteItem(eq(user_id), eq(item_id));

        mockMvc.perform(delete("/items/{itemId}", item_id)
                        .header("X-Sharer-User-Id", user_id))
                .andExpect(status().isNoContent());

        verify(itemService).deleteItem(eq(user_id), eq(item_id));
    }

    @Test
    void addComment_Success() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto("Отличный товар!");
        CommentDto comment = new CommentDto(1L, "Отличный товар!", "User", null);

        when(itemService.addComment(eq(user_id), eq(item_id), any(CommentCreateDto.class)))
                .thenReturn(comment);

        mockMvc.perform(post("/items/{itemId}/comment", item_id)
                        .header("X-Sharer-User-Id", user_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличный товар!"));
    }

    @Test
    void addComment_ValidationFailed_ThrowsException() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto("Отличный товар!");

        when(itemService.addComment(eq(user_id), eq(item_id), any(CommentCreateDto.class)))
                .thenThrow(new ValidationException("Нельзя оставить комментарий"));

        mockMvc.perform(post("/items/{itemId}/comment", item_id)
                        .header("X-Sharer-User-Id", user_id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getComments_Success() throws Exception {
        when(itemService.getComments(eq(item_id)))
                .thenReturn(List.of(new CommentDto(1L, "Коммент", "User", null)));

        mockMvc.perform(get("/items/{itemId}/comments", item_id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Коммент"));
    }
}