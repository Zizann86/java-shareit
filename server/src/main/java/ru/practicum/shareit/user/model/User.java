package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO Sprint add-controllers.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 30, nullable = false)
    @NotBlank
    @Size(max = 30)
    private String name;
    @Column(length = 100, unique = true, nullable = false)
    @NotBlank
    @Size(max = 100)
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
