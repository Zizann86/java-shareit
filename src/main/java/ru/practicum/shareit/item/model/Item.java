package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false)
    @NotBlank
    private String name;
    @Column(length = 500, nullable = false)
    @NotBlank
    private String description;
    @Column(name = "is_available", nullable = false)
    @NotNull
    private Boolean available;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
