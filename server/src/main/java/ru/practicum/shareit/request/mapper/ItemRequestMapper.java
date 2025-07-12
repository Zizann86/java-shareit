package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        dto.setCreated(itemRequest.getCreatedTime());
        if (itemRequest.getRequester() != null) {
            dto.setRequester(UserMapper.toDto(itemRequest.getRequester()));
        }
        dto.setItems(itemRequest.getItems() != null
                ? itemRequest.getItems().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList())
                : new ArrayList<>());
        return dto;
    }

    public static ItemRequest toItemRequest(CreateItemRequestDto createItemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(createItemRequestDto.getDescription());
        return itemRequest;
    }
}
