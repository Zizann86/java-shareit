package ru.practicum.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.booking.dto.BookingState;
import ru.practicum.booking.dto.CreateBookingDto;
import ru.practicum.client.BaseClient;

import java.util.Map;

@Component
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(Long userId, CreateBookingDto createBookingDto) {
        return post("", userId, createBookingDto);
    }

    public ResponseEntity<Object> updateStatusBooking(Long userId, Long bookingId, Boolean approved) {
        return patch(("/" + bookingId + "?approved=" + approved), userId, null);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

   /* public ResponseEntity<Object> getAllBookingsForBooker(Long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state, "from", "size");
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }*/

    public ResponseEntity<Object> getAllBookingsForBooker(Long userId, BookingState state) {
        return get("?state={state}", userId, Map.of("state", state.name()));
    }

    public ResponseEntity<Object> getBookingsForOwner(Long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("/owner", userId, parameters);
    }
}
