package ru.practicum.itemRequest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestDtoJsonTest {
    private final JacksonTester<ItemRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto requester = new UserDto(1L, "Requester", "requester@example.com");
        ItemDto item = new ItemDto(
                1L,
                "Test Item",
                "Test Description",
                true,
                null,
                null,
                null,
                null
        );

        ItemRequestDto dto = new ItemRequestDto(
                1L,
                "Need some items",
                requester,
                LocalDateTime.of(2023, 1, 1, 12, 0),
                List.of(item)
        );

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.requester");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).hasJsonPath("$.items");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need some items");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2023-01-01T12:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.requester.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
    }

    @Test
    void testDeserializeWithItems() throws Exception {
        String jsonString = "{"
                + "\"id\": 1, "
                + "\"description\": \"Need some items\", "
                + "\"requester\": {"
                + "\"id\": 1, "
                + "\"name\": \"Requester\", "
                + "\"email\": \"requester@example.com\""
                + "}, "
                + "\"created\": \"2023-01-01T12:00:00\", "
                + "\"items\": ["
                + "{"
                + "\"id\": 1, "
                + "\"name\": \"Test Item\", "
                + "\"description\": \"Test Description\", "
                + "\"available\": true"
                + "}"
                + "]"
                + "}";

        ItemRequestDto dto = json.parse(jsonString).getObject();

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need some items");
        assertThat(dto.getCreated())
                .isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
        assertThat(dto.getRequester()).isNotNull();
        assertThat(dto.getRequester().getId()).isEqualTo(1L);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void testDeserializeWithoutItems() throws Exception {
        String jsonString = "{"
                + "\"id\": 1, "
                + "\"description\": \"Need some items\", "
                + "\"requester\": {"
                + "\"id\": 1, "
                + "\"name\": \"Requester\", "
                + "\"email\": \"requester@example.com\""
                + "}, "
                + "\"created\": \"2023-01-01T12:00:00\""
                + "}";

        ItemRequestDto dto = json.parse(jsonString).getObject();

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need some items");
        assertThat(dto.getCreated())
                .isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
        assertThat(dto.getRequester()).isNotNull();
        assertThat(dto.getItems()).isNull(); // Изменили ожидание на null
    }

    @Test
    void testDeserializeWithEmptyItems() throws Exception {
        String jsonString = "{"
                + "\"id\": 1, "
                + "\"description\": \"Need some items\", "
                + "\"requester\": {"
                + "\"id\": 1, "
                + "\"name\": \"Requester\", "
                + "\"email\": \"requester@example.com\""
                + "}, "
                + "\"created\": \"2023-01-01T12:00:00\", "
                + "\"items\": []"
                + "}";

        ItemRequestDto dto = json.parse(jsonString).getObject();

        assertThat(dto).isNotNull();
        assertThat(dto.getItems()).isNotNull();
        assertThat(dto.getItems()).isEmpty();
    }
}
