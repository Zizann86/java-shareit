package ru.practicum.itemRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.itemRequest.dto.CreateItemRequestDto;

import static ru.practicum.util.Constants.USER_ID_HEADER;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @RequestBody @Valid CreateItemRequestDto createItemRequestDto) {
        return itemRequestClient.createItemRequest(userId, createItemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsById(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestClient.getAllRequestsById(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @RequestParam(name = "size", defaultValue = "50") Integer size) {
        return itemRequestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable("requestId") Long requestId) {
        return itemRequestClient.findById(userId, requestId);
    }
}
