package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

@Getter @Setter @ToString
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}