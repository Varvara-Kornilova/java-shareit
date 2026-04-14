package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Collection<Item> findAll();

    Optional<Item> findItemById(Long itemId);

    Item save(Item item);

    void delete(Long itemId);
}