package ru.practicum.shareit.booking.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND CURRENT_TIMESTAMP BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentBookings(@Param("bookerId") Long bookerId);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findAllByBookerIdAndStatusInOrderByStartDesc(Long bookerId, List<BookingStatus> statuses);

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND CURRENT_TIMESTAMP BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByOwner(@Param("ownerId") Long ownerId);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByItemId(Long itemId);
}
