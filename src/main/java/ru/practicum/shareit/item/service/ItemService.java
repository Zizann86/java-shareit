package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto findById(Long itemId);

    ItemDto update(Long userId, Long itemId, UpdateItemDto itemDto);

    List<ItemDto> getAllOwnerItems(Long userId);

    List<ItemDto> searchItems(String text);

    Item validateItemExist(Long itemId);
}
