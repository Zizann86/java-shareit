package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.item.mapper.ItemMapper.toItemUpdate;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи");
        User user = userService.validateUserExist(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        item = itemRepository.createItem(item);
        log.info("Предмет создан: {}", item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto findById(Long itemId) {
        log.info("Получение вещи по id : {}", itemId);
        Optional<Item> item = itemRepository.getItemById(itemId);
        if (item.isPresent()) {
            return ItemMapper.toItemDto(item.get());
        } else {
            throw new NotFoundException("Пользователь с данным id не был найден");
        }
    }

    @Override
    public ItemDto update(Long itemId, Long userId, UpdateItemDto itemDto) {
        log.info("Происходит обновление вещи с id : {}, у пользователя по id : {}", itemId, userId);
        userService.validateUserExist(userId);
        Item item = validateItemExist(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Предмет аренды не принадлежит данному пользователю");
        }
        toItemUpdate(item, itemDto);
        itemRepository.updateItem(item);
        return toItemDto(item);
    }

    public List<ItemDto> getAllOwnerItems(Long userId) {
        log.info("Получение всех вещей у пользователя с id : {}", userId);
        userService.validateUserExist(userId);
        List<Item> items = itemRepository.getByIdUserItems(userId);
        return ItemMapper.toDto(items);
    }

    public List<ItemDto> searchItems(String text) {
        log.info("Происходит поиск вещей по запросу : {}", text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return ItemMapper.toDto(itemRepository.searchItems(text));
    }

    @Override
    public Item validateItemExist(Long itemId) {
        return itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Предмет с id %d не найден.", itemId)));
    }
}
