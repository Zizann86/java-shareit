package ru.practicum.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking pastBooking;
    private Booking currentBooking;
    private Booking futureBooking;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User("Owner", "owner@example.com"));
        booker = userRepository.save(new User("Booker", "booker@example.com"));

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        now = LocalDateTime.now();

        pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        pastBooking.setStart(now.minusDays(2));
        pastBooking.setEnd(now.minusDays(1));
        bookingRepository.save(pastBooking);

        currentBooking = new Booking();
        currentBooking.setItem(item);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);
        currentBooking.setStart(now.minusHours(1));
        currentBooking.setEnd(now.plusHours(1));
        bookingRepository.save(currentBooking);

        futureBooking = new Booking();
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.WAITING);
        futureBooking.setStart(now.plusDays(1));
        futureBooking.setEnd(now.plusDays(2));
        bookingRepository.save(futureBooking);
    }

    @Test
    void findAllByItemOwnerIdOrderByStartDesc_shouldReturnAllBookingsForOwner() {
        List<Booking> result = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(owner.getId());

        assertEquals(3, result.size());
        assertEquals(futureBooking.getId(), result.get(0).getId()); // Сортировка по убыванию даты начала
        assertEquals(currentBooking.getId(), result.get(1).getId());
        assertEquals(pastBooking.getId(), result.get(2).getId());
    }

    @Test
    void findAllByItemOwnerIdAndStatusOrderByStartDesc_shouldReturnFilteredBookings() {
        List<Booking> result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                owner.getId(), BookingStatus.APPROVED);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getStatus() == BookingStatus.APPROVED));
    }

    @Test
    void findCurrentBookings_shouldReturnCurrentBookingsForBooker() {
        List<Booking> result = bookingRepository.findCurrentBookings(booker.getId());

        assertEquals(1, result.size());
        assertEquals(currentBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByItemOwnerIdAndStartAfterOrderByStartDesc_shouldReturnFutureBookings() {
        List<Booking> result = bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                owner.getId(), now);

        assertEquals(1, result.size());
        assertEquals(futureBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByItemOwnerIdAndEndBeforeOrderByStartDesc_shouldReturnPastBookings() {
        List<Booking> result = bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                owner.getId(), now);

        assertEquals(1, result.size());
        assertEquals(pastBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByBookerIdOrderByStartDesc_shouldReturnAllBookingsForBooker() {
        List<Booking> result = bookingRepository.findAllByBookerIdOrderByStartDesc(booker.getId());

        assertEquals(3, result.size());
        assertEquals(futureBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByBookerIdAndStatusOrderByStartDesc_shouldReturnFilteredBookings() {
        List<Booking> result = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                booker.getId(), BookingStatus.WAITING);

        assertEquals(1, result.size());
        assertEquals(futureBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByBookerIdAndStatusInOrderByStartDesc_shouldReturnBookingsWithStatuses() {
        List<BookingStatus> statuses = List.of(BookingStatus.APPROVED, BookingStatus.WAITING);
        List<Booking> result = bookingRepository.findAllByBookerIdAndStatusInOrderByStartDesc(
                booker.getId(), statuses);

        assertEquals(3, result.size());
    }

    @Test
    void findCurrentBookingsByOwner_shouldReturnCurrentBookingsForOwner() {
        List<Booking> result = bookingRepository.findCurrentBookingsByOwner(owner.getId());

        assertEquals(1, result.size());
        assertEquals(currentBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByBookerIdAndStartAfterOrderByStartDesc_shouldReturnFutureBookingsForBooker() {
        List<Booking> result = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                booker.getId(), now);

        assertEquals(1, result.size());
        assertEquals(futureBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByBookerIdAndEndBeforeOrderByStartDesc_shouldReturnPastBookingsForBooker() {
        List<Booking> result = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                booker.getId(), now);

        assertEquals(1, result.size());
        assertEquals(pastBooking.getId(), result.get(0).getId());
    }

    @Test
    void findAllByItemId_shouldReturnAllBookingsForItem() {
        List<Booking> result = bookingRepository.findAllByItemId(item.getId());

        assertEquals(3, result.size());
    }
}
