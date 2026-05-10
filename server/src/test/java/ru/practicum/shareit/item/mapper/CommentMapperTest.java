package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toComment_MapsAllFields() {
        CommentCreateDto dto = new CommentCreateDto("Отличный товар!");
        Item item = new Item();
        item.setId(10L);
        User author = new User();
        author.setId(1L);
        author.setName("Иван");

        Comment comment = CommentMapper.toComment(dto, item, author);

        assertEquals("Отличный товар!", comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
        assertNotNull(comment.getCreated());
        assertTrue(comment.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void toCommentDto_MapsAllFields() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Коммент");
        comment.setCreated(LocalDateTime.of(2024, 6, 1, 12, 0));

        User author = new User();
        author.setName("Иван");
        comment.setAuthor(author);

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertEquals(1L, dto.getId());
        assertEquals("Коммент", dto.getText());
        assertEquals("Иван", dto.getAuthorName());
        assertEquals(LocalDateTime.of(2024, 6, 1, 12, 0), dto.getCreated());
    }
}