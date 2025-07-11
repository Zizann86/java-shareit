package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private User author;

    @Column(nullable = false)
    private LocalDateTime created;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(text, comment.text) &&
                Objects.equals(item, comment.item) &&
                Objects.equals(author, comment.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, item, author);
    }
}
