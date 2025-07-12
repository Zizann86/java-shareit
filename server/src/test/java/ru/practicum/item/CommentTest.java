package ru.practicum.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void testCommentCreation() {
        User author = new User(1L, "Author", "author@example.com");
        Item item = new Item(1L, "Item", "Description", true, author, null);
        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        assertEquals(1L, comment.getId());
        assertEquals("Great item!", comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
        assertEquals(now, comment.getCreated());
    }

    @Test
    void testEqualsAndHashCode() {
        User author1 = new User(1L, "Author", "author@example.com");
        Item item1 = new Item(1L, "Item", "Description", true, author1, null);
        Comment comment1 = new Comment(1L, "Text", item1, author1, LocalDateTime.now());

        User author2 = new User(1L, "Author", "author@example.com");
        Item item2 = new Item(1L, "Item", "Description", true, author2, null);
        Comment comment2 = new Comment(2L, "Text", item2, author2, LocalDateTime.now().plusHours(1));

        Comment comment3 = new Comment(3L, "Different", item1, author1, LocalDateTime.now());

        assertEquals(comment1, comment2);
        assertEquals(comment1.hashCode(), comment2.hashCode());

        assertNotEquals(comment1, comment3);
        assertNotEquals(comment1.hashCode(), comment3.hashCode());
    }

    @Test
    void testToString() {
        User author = new User(1L, "Author", "author@example.com");
        Item item = new Item(1L, "Item", "Description", true, author, null);
        Comment comment = new Comment(1L, "Text", item, author, LocalDateTime.now());

        String toStringResult = comment.toString();

        assertTrue(toStringResult.contains("Text"));
        assertTrue(toStringResult.contains("Item"));

        System.out.println("Actual toString(): " + toStringResult);
    }
}
