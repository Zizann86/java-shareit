package ru.practicum.itemRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.itemRequest.dto.CreateItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody @Valid CreateItemRequestDto createItemRequestDto) {
        return itemRequestClient.createItemRequest(userId, createItemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsById(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllRequestsById(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @RequestParam(name = "size", defaultValue = "50") Integer size) {
        return itemRequestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable("requestId") Long requestId) {
        return itemRequestClient.findById(userId, requestId);
    }
}
