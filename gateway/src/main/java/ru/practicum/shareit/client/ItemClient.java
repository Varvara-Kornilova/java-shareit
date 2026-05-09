package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collections;

@Component
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> addItem(long ownerId, ItemDto itemDto) {
        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> updateItem(long itemId, long ownerId, ItemUpdateDto itemDto) {
        return patch("/" + itemId, ownerId, null, itemDto);
    }

    public ResponseEntity<Object> deleteItem(long itemId, long ownerId) {
        return delete("/" + itemId, ownerId, null);
    }

    public ResponseEntity<Object> getItem(long itemId) {
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getAllItems(long ownerId) {
        return get("", ownerId);
    }

    public ResponseEntity<Object> search(String text) {
        if (text == null || text.isBlank()) {
            return get("/search", null, Collections.emptyMap());
        }
        return get("/search?text={text}", null, Collections.singletonMap("text", text));
    }

    public ResponseEntity<Object> addComment(long itemId, long ownerId, CommentCreateDto comment) {
        return post("/" + itemId + "/comment", ownerId, comment);
    }

    public ResponseEntity<Object> getComments(long itemId) {
        return get("/" + itemId + "/comments");
    }
}