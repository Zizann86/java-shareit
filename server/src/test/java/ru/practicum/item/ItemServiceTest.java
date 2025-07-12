package ru.practicum.item;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dal.CommentRepository;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final Long userId = 1L;
    private final Long itemId = 1L;
    private final Long requestId = 1L;
    private final Long commentId = 1L;
    private final Long bookingId = 1L;

    private final User user = new User(userId, "John", "john@example.com");
    private final ItemRequest itemRequest = new ItemRequest("Need item", user, LocalDateTime.now());
    private final ItemDto inputItemDto = new ItemDto(null, "Item", "Description", true, requestId, null, null, null);
    private final Item itemToSave = new Item(null, "Item", "Description", true, user, itemRequest);
    private final Item savedItem = new Item(itemId, "Item", "Description", true, user, itemRequest);
    private final UpdateItemDto updateItemDto = new UpdateItemDto(itemId, "Updated", "New Description", false, null);
    private final Item updatedItem = new Item(itemId, "Updated", "New Description", false, user, null);
    private final CreateCommentDto createCommentDto = new CreateCommentDto("Test comment");
    private final Comment comment = new Comment(commentId, "Test comment", savedItem, user, LocalDateTime.now());
    private final Booking booking = new Booking(bookingId, LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusDays(1), savedItem, user, BookingStatus.APPROVED);

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userService, bookingRepository,
                commentRepository, itemRequestRepository);
    }

    @Test
    void create_shouldCreateNewItem() {
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(itemToSave)).thenReturn(savedItem);

        ItemDto result = itemService.create(userId, inputItemDto);

        assertEquals(itemId, result.getId());
        assertEquals("Item", result.getName());
        verify(itemRepository).save(itemToSave);
    }

    @Test
    void create_shouldCreateItemWithoutRequest() {
        ItemDto dtoWithoutRequest = new ItemDto(null, "Item", "Description", true, null, null, null, null);
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.create(userId, dtoWithoutRequest);

        assertNotNull(result);
        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        when(userService.validateUserExist(userId)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> itemService.create(userId, inputItemDto));
    }

    @Test
    void create_shouldThrowWhenRequestNotFound() {
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(userId, inputItemDto));
    }

    @Test
    void findById_shouldReturnItem() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.emptyList());
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(itemId)).thenReturn(Collections.emptyList());

        ItemDto result = itemService.findById(itemId);

        assertEquals(itemId, result.getId());
        assertEquals("Item", result.getName());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void findById_shouldReturnItemWithCommentsAndBookings() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(itemId)).thenReturn(List.of(booking));

        ItemDto result = itemService.findById(itemId);

        assertEquals(itemId, result.getId());
        assertEquals(1, result.getComments().size());
        assertEquals(bookingId, result.getLastBooking().getId());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(itemId));
    }

    @Test
    void update_shouldUpdateFields() {
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(itemRepository.save(updatedItem)).thenReturn(updatedItem);

        ItemDto result = itemService.update(itemId, userId, updateItemDto);

        assertEquals(itemId, result.getId());
        assertEquals("Updated", result.getName());
        assertEquals("New Description", result.getDescription());
        verify(itemRepository).save(updatedItem);
    }

    @Test
    void update_shouldUpdateRequestId() {
        UpdateItemDto dtoWithRequest = new UpdateItemDto(itemId, null, null, null, requestId);
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any())).thenReturn(savedItem);

        ItemDto result = itemService.update(itemId, userId, dtoWithRequest);

        assertNotNull(result);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void update_shouldRemoveRequestId() {
        savedItem.setItemRequest(itemRequest);
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(itemRepository.save(any())).thenReturn(savedItem);

        ItemDto result = itemService.update(itemId, userId, updateItemDto);

        assertNotNull(result);
        assertNull(savedItem.getItemRequest());
    }

    @Test
    void update_shouldThrowWhenNotOwner() {
        Long otherUserId = 2L;
        when(userService.validateUserExist(otherUserId)).thenReturn(new User(otherUserId, "Other", "other@example.com"));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

        assertThrows(NotFoundException.class,
                () -> itemService.update(itemId, otherUserId, updateItemDto));
    }

    @Test
    void update_shouldThrowWhenRequestNotFound() {
        UpdateItemDto dtoWithRequest = new UpdateItemDto(itemId, null, null, null, requestId);
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.update(itemId, userId, dtoWithRequest));
    }

    @Test
    void getAllOwnerItems_shouldReturnList() {
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findAllByOwnerId(userId)).thenReturn(List.of(savedItem));
        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.emptyList());
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(itemId)).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getAllOwnerItems(userId);

        assertEquals(1, result.size());
        assertEquals(itemId, result.get(0).getId());
    }

    @Test
    void searchItems_shouldReturnEmptyListForBlankText() {
        List<ItemDto> result = itemService.searchItems(" ");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_shouldReturnMatchingItems() {
        String searchText = "test";
        when(itemRepository.search(searchText)).thenReturn(List.of(savedItem));

        List<ItemDto> result = itemService.searchItems(searchText);

        assertEquals(1, result.size());
        assertEquals(itemId, result.get(0).getId());
    }

    @Test
    void addComment_shouldCreateComment() {
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findAllByItemId(itemId)).thenReturn(List.of(booking));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDto result = itemService.addComment(userId, itemId, createCommentDto);

        assertEquals(commentId, result.getId());
        assertEquals("Test comment", result.getText());
    }

    @Test
    void addComment_shouldThrowWhenNoBooking() {
        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findAllByItemId(itemId)).thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class,
                () -> itemService.addComment(userId, itemId, createCommentDto));
    }

    @Test
    void addComment_shouldThrowWhenNoApprovedBooking() {
        Booking waitingBooking = new Booking(bookingId, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), savedItem, user, BookingStatus.WAITING);

        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findAllByItemId(itemId)).thenReturn(List.of(waitingBooking));

        assertThrows(ValidationException.class,
                () -> itemService.addComment(userId, itemId, createCommentDto));
    }

    @Test
    void addComment_shouldThrowWhenBookingNotEnded() {
        Booking currentBooking = new Booking(bookingId, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), savedItem, user, BookingStatus.APPROVED);

        when(userService.validateUserExist(userId)).thenReturn(user);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findAllByItemId(itemId)).thenReturn(List.of(currentBooking));

        assertThrows(ValidationException.class,
                () -> itemService.addComment(userId, itemId, createCommentDto));
    }

    @Test
    void validateItemExist_shouldReturnItem() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

        Item result = itemService.validateItemExist(itemId);

        assertEquals(savedItem, result);
    }

    @Test
    void validateItemExist_shouldThrowWhenNotFound() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.validateItemExist(itemId));
    }
}
