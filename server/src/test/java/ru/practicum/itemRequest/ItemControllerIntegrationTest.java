package ru.practicum.itemRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(new User("Test User", "test@user.com"));
    }

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(itemDto)))
                .andExpect(status().isOk()) // Измените на isOk() если контроллер возвращает 200
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getItem_shouldReturnItem() throws Exception {
        Item savedItem = itemRepository.save(
                new Item("Existing Item", "Existing Description", true, testUser, null));

        mockMvc.perform(get("/items/{itemId}", savedItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedItem.getId()))
                .andExpect(jsonPath("$.name").value("Existing Item"))
                .andExpect(jsonPath("$.description").value("Existing Description"));
    }

    @Test
    void updateItem_shouldUpdateFields() throws Exception {
        Item savedItem = itemRepository.save(
                new Item("Old Name", "Old Description", true, testUser, null));
        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName("New Name");
        updateDto.setDescription("New Description");
        updateDto.setAvailable(false);

        mockMvc.perform(patch("/items/{itemId}", savedItem.getId())
                        .header("X-Sharer-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getUserItems_shouldReturnUserItems() throws Exception {
        itemRepository.save(new Item("Item 1", "Desc 1", true, testUser, null));
        itemRepository.save(new Item("Item 2", "Desc 2", false, testUser, null));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[1].name").value("Item 2"));
    }

    @Test
    void searchItems_shouldReturnMatchingItems() throws Exception {
        itemRepository.save(new Item("Drill", "Powerful drill", true, testUser, null));
        itemRepository.save(new Item("Hammer", "Heavy hammer", true, testUser, null));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
