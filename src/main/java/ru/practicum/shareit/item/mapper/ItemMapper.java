package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                new ArrayList<>()
        );
    }

    public static Item toItem(ItemDto itemDto, User owner) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        return item;
    }

    public static List<ItemDto> toDto(List<Item> items) {
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public static Item toItemUpdate(Item item, UpdateItemDto updateItemRequest) {
        if (updateItemRequest.hasName()) {
            item.setName(updateItemRequest.getName());
        }
        if (updateItemRequest.hasDescription()) {
            item.setDescription(updateItemRequest.getDescription());
        }
        if (updateItemRequest.hasAvailable()) {
            item.setAvailable(updateItemRequest.getAvailable());
        }
        return item;
    }
}
