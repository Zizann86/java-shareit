package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createItemRequest(Long userId, CreateItemRequestDto createItemRequestDto);

    List<ItemRequestDto> getAllRequestsById(Long userId);

    List<ItemRequestDto> findAll(Long userId, Integer from, Integer size);

    ItemRequestDto findById(Long userId, Long requestId);
}
