package ru.practicum.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServicelmpl;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    private BookingService bookingService;

    private final Long userId = 1L;
    private final Long ownerId = 2L;
    private final Long bookingId = 1L;
    private final Long itemId = 1L;
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime start = now.plusDays(1);
    private final LocalDateTime end = now.plusDays(2);

    private final User booker = new User(userId, "Booker", "booker@example.com");
    private final User owner = new User(ownerId, "Owner", "owner@example.com");
    private final User unrelatedUser = new User(3L, "Unrelated", "unrelated@example.com");
    private final Item item = new Item(itemId, "Item", "Description", true, owner, null);
    private final Item ownItem = new Item(itemId, "Item", "Description", true, booker, null);
    private final Item unavailableItem = new Item(itemId, "Item", "Description", false, owner, null);
    private final CreateBookingDto createBookingDto = new CreateBookingDto(itemId, start, end);
    private final Booking booking;

    public BookingServiceTest() {
        booking = new Booking();
        booking.setId(bookingId);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
    }

    @BeforeEach
    void setUp() {
        bookingService = new BookingServicelmpl(bookingRepository, itemRepository, userRepository);
    }

    @Test
    void create_shouldCreateNewBooking() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Booking expectedBooking = new Booking();
        expectedBooking.setStart(start);
        expectedBooking.setEnd(end);
        expectedBooking.setItem(item);
        expectedBooking.setBooker(booker);
        expectedBooking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.save(expectedBooking)).thenReturn(booking);

        BookingDto result = bookingService.create(userId, createBookingDto);

        assertEquals(bookingId, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(expectedBooking);
    }

    @Test
    void create_shouldThrowWhenItemNotAvailable() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(unavailableItem));

        assertThrows(IllegalStateException.class,
                () -> bookingService.create(userId, createBookingDto));
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.create(userId, createBookingDto));
    }

    @Test
    void create_shouldThrowWhenItemNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.create(userId, createBookingDto));
    }

    @Test
    void create_shouldThrowWhenBookingOwnItem() {
        Item ownItem = new Item(itemId, "Item", "Description", true, booker, null);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(ownItem));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(userId, createBookingDto));

        assertEquals("Нельзя забронировать свою собственную вещь", exception.getMessage());

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateStatusBooking_shouldApproveBooking() {
        Booking waitingBooking = new Booking();
        waitingBooking.setId(bookingId);
        waitingBooking.setStatus(BookingStatus.WAITING);
        waitingBooking.setItem(item);
        waitingBooking.setBooker(booker);

        Booking approvedBooking = new Booking();
        approvedBooking.setId(bookingId);
        approvedBooking.setStatus(BookingStatus.APPROVED);
        approvedBooking.setItem(item);
        approvedBooking.setBooker(booker);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(waitingBooking));
        when(bookingRepository.save(approvedBooking)).thenReturn(approvedBooking);

        BookingDto result = bookingService.updateStatusBooking(ownerId, bookingId, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository).save(approvedBooking);
    }

    @Test
    void updateStatusBooking_shouldRejectBooking() {
        Booking waitingBooking = new Booking();
        waitingBooking.setId(bookingId);
        waitingBooking.setStatus(BookingStatus.WAITING);
        waitingBooking.setItem(item);
        waitingBooking.setBooker(booker);

        Booking rejectedBooking = new Booking();
        rejectedBooking.setId(bookingId);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(booker);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(waitingBooking));
        when(bookingRepository.save(rejectedBooking)).thenReturn(rejectedBooking);

        BookingDto result = bookingService.updateStatusBooking(ownerId, bookingId, false);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
        verify(bookingRepository).save(rejectedBooking);
    }

    @Test
    void updateStatusBooking_shouldThrowWhenNotOwner() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class,
                () -> bookingService.updateStatusBooking(userId, bookingId, true));
    }

    @Test
    void updateStatusBooking_shouldThrowWhenBookingNotFound() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.updateStatusBooking(ownerId, bookingId, true));
    }

    @Test
    void updateStatusBooking_shouldThrowWhenStatusNotWaiting() {
        Booking approvedBooking = new Booking();
        approvedBooking.setId(bookingId);
        approvedBooking.setStatus(BookingStatus.APPROVED);
        approvedBooking.setItem(item);
        approvedBooking.setBooker(booker);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(approvedBooking));

        assertThrows(IllegalStateException.class,
                () -> bookingService.updateStatusBooking(ownerId, bookingId, true));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBookingById_shouldReturnBookingForBooker() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(userId, bookingId);

        assertEquals(bookingId, result.getId());
    }

    @Test
    void getBookingById_shouldReturnBookingForOwner() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(ownerId, bookingId);

        assertEquals(bookingId, result.getId());
    }

    @Test
    void getBookingById_shouldThrowWhenBookingNotFound() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));
    }

    @Test
    void getBookingById_shouldThrowWhenNotRelated() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(unrelatedUser.getId(), bookingId));
    }

    @Test
    void getBookingsByState_shouldReturnAllBookings() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(userId))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByState(userId, BookingState.ALL);

        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
    }

    @Test
    void getBookingsByState_shouldReturnWaitingBookings() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByState(userId, BookingState.WAITING);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByState_shouldReturnRejectedBookings() {
        booking.setStatus(BookingStatus.REJECTED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusInOrderByStartDesc(userId,
                List.of(BookingStatus.REJECTED, BookingStatus.CANCELED)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByState(userId, BookingState.REJECTED);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByState_shouldReturnCurrentBookings() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findCurrentBookings(userId)).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByState(userId, BookingState.CURRENT);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsByState_shouldReturnFutureBookings() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByState(userId, BookingState.FUTURE);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsForOwner_shouldReturnAllBookings() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsForOwner(ownerId, BookingState.ALL);

        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
    }

    @Test
    void getBookingsForOwner_shouldThrowWhenNoItems() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsForOwner(ownerId, BookingState.ALL));
    }

    @Test
    void getBookingsForOwner_shouldReturnWaitingBookings() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsForOwner(ownerId, BookingState.WAITING);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsForOwner_shouldReturnRejectedBookings() {
        booking.setStatus(BookingStatus.REJECTED);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsForOwner(ownerId, BookingState.REJECTED);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsForOwner_shouldReturnCurrentBookings() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item));
        when(bookingRepository.findCurrentBookingsByOwner(ownerId)).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsForOwner(ownerId, BookingState.CURRENT);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsForOwner_shouldReturnFutureBookings() {
        LocalDateTime fixedNow = LocalDateTime.now();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item));

        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                eq(ownerId),
                any(LocalDateTime.class)
        )).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsForOwner(ownerId, BookingState.FUTURE);

        assertEquals(1, result.size());
    }

    @Test
    void getBookingsForOwner_shouldReturnPastBookings() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item));

        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                eq(ownerId),
                any(LocalDateTime.class)
        )).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsForOwner(ownerId, BookingState.PAST);

        assertEquals(1, result.size());
    }
}
