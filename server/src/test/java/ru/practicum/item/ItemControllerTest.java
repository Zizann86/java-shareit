package ru.practicum.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    private final Long userId = 1L;
    private final Long itemId = 1L;
    private final Long commentId = 1L;

    private final ItemDto inputItemDto = new ItemDto(null, "Item name", "Item description", true, null, null, null, null);
    private final ItemDto outputItemDto = new ItemDto(itemId, "Item name", "Item description", true, null, null, null, null);
    private final UpdateItemDto updateItemDto = new UpdateItemDto(itemId, "Updated name", "Updated description", false, null);
    private final ItemDto updatedItemDto = new ItemDto(itemId, "Updated name", "Updated description", false, null, null, null, null);
    private final CreateCommentDto createCommentDto = new CreateCommentDto("Test comment");
    private final CommentDto commentDto = new CommentDto(commentId, "Test comment", "Author", LocalDateTime.now());

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        when(itemService.create(userId, inputItemDto)).thenReturn(outputItemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(inputItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Item name"))
                .andExpect(jsonPath("$.description").value("Item description"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService).create(userId, inputItemDto);
    }

    @Test
    void getItem_shouldReturnItemById() throws Exception {
        when(itemService.findById(itemId)).thenReturn(outputItemDto);

        mvc.perform(get("/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Item name"))
                .andExpect(jsonPath("$.description").value("Item description"));

        verify(itemService).findById(itemId);
    }

    @Test
    void getAllUserItems_shouldReturnAllItemsForUser() throws Exception {
        when(itemService.getAllOwnerItems(userId)).thenReturn(List.of(outputItemDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[0].name").value("Item name"))
                .andExpect(jsonPath("$[0].description").value("Item description"));

        verify(itemService).getAllOwnerItems(userId);
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        when(itemService.update(itemId, userId, updateItemDto)).thenReturn(updatedItemDto);

        mvc.perform(patch("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(updateItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.available").value(false));

        verify(itemService).update(itemId, userId, updateItemDto);
    }

    @Test
    void searchItems_shouldReturnMatchingItems() throws Exception {
        String searchText = "text";
        when(itemService.searchItems(searchText)).thenReturn(List.of(outputItemDto));

        mvc.perform(get("/items/search")
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[0].name").value("Item name"))
                .andExpect(jsonPath("$[0].description").value("Item description"));

        verify(itemService).searchItems(searchText);
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        when(itemService.addComment(userId, itemId, createCommentDto)).thenReturn(commentDto);

        mvc.perform(post("/items/" + itemId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(createCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.authorName").value("Author"));

        verify(itemService).addComment(userId, itemId, createCommentDto);
    }
}
