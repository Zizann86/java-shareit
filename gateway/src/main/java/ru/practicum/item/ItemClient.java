package ru.practicum.item;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.client.BaseClient;
import ru.practicum.item.dto.CreateCommentDto;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.UpdateItemDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(Long userId, ItemDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, UpdateItemDto updateItemDto) {
        return patch("/" + itemId, userId, updateItemDto);
    }

    public ResponseEntity<Object> findById(Long itemId) {
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getAllOwnerItems(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> searchItems(String text) {
        return get("/search?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8));
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CreateCommentDto createCommentDto) {
        return post(("/" + itemId + "/comment"), userId, createCommentDto);
    }

}
