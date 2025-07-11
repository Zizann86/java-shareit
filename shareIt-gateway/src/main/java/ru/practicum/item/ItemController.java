package ru.practicum.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.item.dto.CreateCommentDto;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.UpdateItemDto;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен HTTP-запрос на добавление вещи: {}", itemDto);
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@Valid @RequestBody UpdateItemDto itemDto,
                              @PathVariable Long itemId,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен HTTP-запрос на обновление вещи: {}", itemDto);
        return itemClient.update(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable Long itemId) {
        log.info("Получен HTTP-запрос на получение вещи по id: {}", itemId);
        return itemClient.findById(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен HTTP-запрос на получение всех вещей у пользователя с id: {}", userId);
        return itemClient.getAllOwnerItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        log.info("Получен HTTP-запрос на поиск вещи по тексту: {}", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable("itemId") Long itemId,
                                             @Valid @RequestBody CreateCommentDto createCommentDto
    ) {
        return itemClient.addComment(userId, itemId, createCommentDto);
    }
}
