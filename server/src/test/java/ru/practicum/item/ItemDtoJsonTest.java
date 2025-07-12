package ru.practicum.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoJsonTest {
    private final JacksonTester<ItemDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto booker = new UserDto(2L, "Booker", "booker@example.com");
        ItemDto item = new ItemDto(1L, "Test Item", "Test Description", true, null, null, null, null);

        LocalDateTime now = LocalDateTime.now();
        BookingDto lastBooking = new BookingDto(
                1L,
                now.minusDays(2),
                now.minusDays(1),
                item,
                booker,
                BookingStatus.APPROVED
        );

        BookingDto nextBooking = new BookingDto(
                2L,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );

        CommentDto comment = new CommentDto(
                1L,
                "Great item!",
                "User1",
                now.minusHours(1)
        );

        ItemDto dto = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                5L,
                lastBooking,
                nextBooking,
                List.of(comment)
        );

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.available");
        assertThat(result).hasJsonPath("$.requestId");
        assertThat(result).hasJsonPath("$.lastBooking");
        assertThat(result).hasJsonPath("$.nextBooking");
        assertThat(result).hasJsonPath("$.comments");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.status")
                .isEqualTo("APPROVED");

        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.status")
                .isEqualTo("WAITING");

        assertThat(result).extractingJsonPathStringValue("$.comments[0].text")
                .isEqualTo("Great item!");
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonString = "{"
                + "\"id\": 1, "
                + "\"name\": \"Test Item\", "
                + "\"description\": \"Test Description\", "
                + "\"available\": true, "
                + "\"requestId\": 5, "
                + "\"lastBooking\": {"
                + "\"id\": 1, "
                + "\"start\": \"2023-01-01T10:00:00\", "
                + "\"end\": \"2023-01-05T10:00:00\", "
                + "\"item\": {"
                + "\"id\": 1, "
                + "\"name\": \"Test Item\", "
                + "\"description\": \"Test Description\", "
                + "\"available\": true, "
                + "\"requestId\": 5"
                + "}, "
                + "\"booker\": {"
                + "\"id\": 2, "
                + "\"name\": \"Booker\", "
                + "\"email\": \"booker@example.com\""
                + "}, "
                + "\"status\": \"APPROVED\""
                + "}, "
                + "\"nextBooking\": {"
                + "\"id\": 2, "
                + "\"start\": \"2023-02-01T10:00:00\", "
                + "\"end\": \"2023-02-05T10:00:00\", "
                + "\"item\": {"
                + "\"id\": 1, "
                + "\"name\": \"Test Item\", "
                + "\"description\": \"Test Description\", "
                + "\"available\": true, "
                + "\"requestId\": 5"
                + "}, "
                + "\"booker\": {"
                + "\"id\": 2, "
                + "\"name\": \"Booker\", "
                + "\"email\": \"booker@example.com\""
                + "}, "
                + "\"status\": \"WAITING\""
                + "}, "
                + "\"comments\": ["
                + "{"
                + "\"id\": 1, "
                + "\"text\": \"Great item!\", "
                + "\"authorName\": \"User1\", "
                + "\"created\": \"2023-01-10T12:00:00\""
                + "}"
                + "]"
                + "}";

        ItemDto dto = json.parse(jsonString).getObject();

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Item");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(5L);

        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(1L);
        assertThat(dto.getLastBooking().getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(dto.getLastBooking().getItem().getName()).isEqualTo("Test Item");

        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getId()).isEqualTo(2L);
        assertThat(dto.getNextBooking().getStatus()).isEqualTo(BookingStatus.WAITING);

        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Great item!");
        assertThat(dto.getComments().get(0).getAuthorName()).isEqualTo("User1");
    }
}
