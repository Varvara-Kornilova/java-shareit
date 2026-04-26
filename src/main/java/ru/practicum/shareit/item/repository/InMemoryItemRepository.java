package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final List<Item> items = new ArrayList<>();
    private long nextId = 1;

    @Override
    public Collection<Item> findAll() {
        return List.copyOf(items);
    }

    @Override
    public Optional<Item> findItemById(Long itemId) {
        return items.stream()
                .filter(item -> item.getId() != null && item.getId().equals(itemId))
                .findFirst();
    }

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        } else {
            items.removeIf(existing -> existing.getId().equals(item.getId()));
        }

        items.add(item);
        return item;
    }

    @Override
    public void delete(Long itemId) {
        items.removeIf(item -> item.getId() != null && item.getId().equals(itemId));
    }
}