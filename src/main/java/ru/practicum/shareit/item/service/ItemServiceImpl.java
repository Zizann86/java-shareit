package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dal.CommentRepository;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
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
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи");
        User user = userService.validateUserExist(userId);
        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
        }
        Item item = ItemMapper.toItem(itemDto, user);
        item.setItemRequest(itemRequest);
        item = itemRepository.save(item);
        log.info("Предмет создан: {}", item);
        ItemDto result = ItemMapper.toItemDto(item);
        getComments(result);
        return result;
    }

    @Override
    public ItemDto findById(Long itemId) {
        log.info("Получение вещи по id : {}", itemId);
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            ItemDto itemDto = ItemMapper.toItemDto(item.get());
            getComments(itemDto);
            return itemDto;
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
        if (itemDto.hasName()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.hasDescription()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.hasAvailable()) {
            item.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.hasRequestId()) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
            item.setItemRequest(itemRequest);
        } else {
            item.setItemRequest(null);
        }
        itemRepository.save(item);
        return toItemDto(item);
    }

    public List<ItemDto> getAllOwnerItems(Long userId) {
        log.info("Получение всех вещей у пользователя с id : {}", userId);
        userService.validateUserExist(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemDto> itemDtos = ItemMapper.toDto(items);
        itemDtos.forEach(this::getComments);
        return itemDtos;
    }

    public List<ItemDto> searchItems(String text) {
        log.info("Происходит поиск вещей по запросу : {}", text);
        if (text == null || text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        return ItemMapper.toDto(itemRepository.search(text));
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CreateCommentDto createCommentDto) {
        Item item = validateItemExist(itemId);
        User user = userService.validateUserExist(userId);
        List<Booking> bookings = bookingRepository.findAllByItemId(itemId);
        Booking booking = bookings.stream()
                .filter(booking1 -> booking1.getBooker().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь не может оставлять комментарий, " +
                        "не являясь арендатором!"));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new ValidationException("Пользователь не имеет подтвержденного бронирования!");
        }

        if (!booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Пользователь не имеет завершенного бронирования!");
        }
        Comment comment = CommentMapper.toComment(createCommentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }


    public Item validateItemExist(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Предмет с id %d не найден.", itemId)));
    }

    private void getComments(ItemDto itemDto) {
        List<Comment> comments = commentRepository.findByItemId(itemDto.getId());
                itemDto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());

        List<Booking> bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(itemDto.getId());

        if (!bookings.isEmpty()) {
            Booking nextBooking = bookings.get(0);
            itemDto.setNextBooking(BookingMapper.toBookingDto(nextBooking));

            if (bookings.size() > 1) {
                itemDto.setLastBooking(BookingMapper.toBookingDto(bookings.get(1)));
            } else {
                itemDto.setLastBooking(BookingMapper.toBookingDto(nextBooking));
            }
        }
    }
}
