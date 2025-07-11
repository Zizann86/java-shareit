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
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private final Item item = new Item(itemId, "Item", "Description", true, owner, null);
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
        Item unavailableItem = new Item(itemId, "Item", "Description", false, owner, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(unavailableItem));

        assertThrows(IllegalStateException.class,
                () -> bookingService.create(userId, createBookingDto));
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
    void getBookingById_shouldReturnBookingForBooker() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(userId, bookingId);

        assertEquals(bookingId, result.getId());
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
    void getBookingsForOwner_shouldReturnAllBookings() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(item));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsForOwner(ownerId, BookingState.ALL);

        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
    }
}
