package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Getter
@Setter
@Entity
@Table(name = "requests")
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requester;
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    @OneToMany(mappedBy = "itemRequest")
    List<Item> items;

    public ItemRequest(String description, User requester, LocalDateTime createdTime) {
        this.description = description;
        this.requester = requester;
        this.createdTime = createdTime;
    }
}
