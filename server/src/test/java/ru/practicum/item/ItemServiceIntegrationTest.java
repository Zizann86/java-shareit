package ru.practicum.item;

import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dal.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Slf4j
public class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private ItemRequest request;
    private Item item;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();

        owner = userRepository.save(new User("Owner", "owner@mail.ru"));
        booker = userRepository.save(new User("Booker", "booker@mail.ru"));
        request = itemRequestRepository.save(new ItemRequest("Need item", booker, LocalDateTime.now()));
        item = itemRepository.save(new Item("Item", "Description", true, owner, null));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void create_shouldSaveItemToDatabase() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);
        itemDto.setRequestId(request.getId());

        ItemDto result = itemService.create(owner.getId(), itemDto);

        assertNotNull(result.getId());
        assertEquals("Drill", result.getName());
        assertEquals(request.getId(), result.getRequestId());

        Item savedItem = itemRepository.findById(result.getId()).orElseThrow();
        assertEquals("Drill", savedItem.getName());
        assertEquals(owner.getId(), savedItem.getOwner().getId());
    }

    @Test
    void create_shouldSaveItemWithoutRequest() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);

        ItemDto result = itemService.create(owner.getId(), itemDto);

        assertNotNull(result.getId());
        assertNull(result.getRequestId());
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);

        assertThrows(NotFoundException.class,
                () -> itemService.create(999L, itemDto));
    }

    @Test
    void create_shouldThrowWhenRequestNotFound() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);
        itemDto.setRequestId(999L);

        assertThrows(NotFoundException.class,
                () -> itemService.create(owner.getId(), itemDto));
    }

    @Test
    void findById_shouldReturnItemWithComments() {
        User freshBooker = userRepository.findById(booker.getId()).orElseThrow();
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        Comment comment = new Comment();
        comment.setText("Great item!");
        comment.setItem(freshItem);
        comment.setAuthor(freshBooker);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        ItemDto result = itemService.findById(freshItem.getId());

        assertEquals(freshItem.getId(), result.getId());
        assertEquals(1, result.getComments().size());
        assertEquals("Great item!", result.getComments().get(0).getText());
    }

    @Test
    void findById_shouldThrowWhenItemNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemService.findById(999L));
    }

    @Test
    void update_shouldUpdateItemFields() {
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName("New");
        updateDto.setDescription("New desc");
        updateDto.setAvailable(false);

        ItemDto result = itemService.update(freshItem.getId(), owner.getId(), updateDto);

        assertEquals("New", result.getName());
        assertEquals("New desc", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void update_shouldUpdateRequestId() {
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setRequestId(request.getId());

        ItemDto result = itemService.update(freshItem.getId(), owner.getId(), updateDto);

        assertEquals(request.getId(), result.getRequestId());
    }

    @Test
    void update_shouldRemoveRequestId() {
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();
        freshItem.setItemRequest(request);
        itemRepository.save(freshItem);

        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setRequestId(null);

        ItemDto result = itemService.update(freshItem.getId(), owner.getId(), updateDto);

        assertNull(result.getRequestId());
    }

    @Test
    void update_shouldThrowWhenNotOwner() {
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName("New");

        assertThrows(NotFoundException.class,
                () -> itemService.update(freshItem.getId(), booker.getId(), updateDto));
    }

    @Test
    void update_shouldThrowWhenRequestNotFound() {
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setRequestId(999L);

        assertThrows(NotFoundException.class,
                () -> itemService.update(freshItem.getId(), owner.getId(), updateDto));
    }

    @Test
    void getAllOwnerItems_shouldReturnAllItemsWithComments() {
        User freshBooker = userRepository.findById(booker.getId()).orElseThrow();
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        Comment comment = new Comment();
        comment.setText("Comment");
        comment.setItem(freshItem);
        comment.setAuthor(freshBooker);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        List<ItemDto> result = itemService.getAllOwnerItems(owner.getId());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getComments().size());
    }

    @Test
    void searchItems_shouldReturnMatchingItems() {
        userRepository.findById(owner.getId()).orElseThrow();
        itemRepository.save(new Item("Drill", "Power tool", true, owner, null));
        itemRepository.save(new Item("Hammer", "Hand tool", true, owner, null));

        List<ItemDto> result = itemService.searchItems("drill");

        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void searchItems_shouldReturnEmptyListForBlankText() {
        userRepository.findById(owner.getId()).orElseThrow();
        itemRepository.save(new Item("Drill", "Power tool", true, owner, null));

        List<ItemDto> result = itemService.searchItems(" ");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_shouldReturnEmptyListForUnavailableItems() {
        userRepository.findById(owner.getId()).orElseThrow();
        itemRepository.save(new Item("Drill", "Power tool", false, owner, null));

        List<ItemDto> result = itemService.searchItems("drill");

        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_shouldSaveComment() {
        User freshBooker = userRepository.findById(booker.getId()).orElseThrow();
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setItem(freshItem);
        booking.setBooker(freshBooker);
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CreateCommentDto commentDto = new CreateCommentDto("Great item!");
        CommentDto result = itemService.addComment(freshBooker.getId(), freshItem.getId(), commentDto);

        assertNotNull(result.getId());
        assertEquals("Great item!", result.getText());
    }

    @Test
    void addComment_shouldThrowWhenNoBooking() {
        User freshBooker = userRepository.findById(booker.getId()).orElseThrow();
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        CreateCommentDto commentDto = new CreateCommentDto("Great item!");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(freshBooker.getId(), freshItem.getId(), commentDto));
    }

    @Test
    void addComment_shouldThrowWhenBookingNotApproved() {
        User freshBooker = userRepository.findById(booker.getId()).orElseThrow();
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setItem(freshItem);
        booking.setBooker(freshBooker);
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);

        CreateCommentDto commentDto = new CreateCommentDto("Great item!");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(freshBooker.getId(), freshItem.getId(), commentDto));
    }

    @Test
    void addComment_shouldThrowWhenBookingNotEnded() {
        User freshBooker = userRepository.findById(booker.getId()).orElseThrow();
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(freshItem);
        booking.setBooker(freshBooker);
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CreateCommentDto commentDto = new CreateCommentDto("Great item!");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(freshBooker.getId(), freshItem.getId(), commentDto));
    }

    @Test
    void validateItemExist_shouldReturnItemWhenExists() {
        Item freshItem = itemRepository.findById(item.getId()).orElseThrow();
        Item result = itemService.validateItemExist(freshItem.getId());

        assertEquals(freshItem.getId(), result.getId());
    }

    @Test
    void validateItemExist_shouldThrowWhenNotExists() {
        assertThrows(NotFoundException.class,
                () -> itemService.validateItemExist(999L));
    }
}
