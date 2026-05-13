package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.status = 'APPROVED' AND b.end < :now")
    boolean hasApprovedPastBooking(Long userId, Long itemId, java.time.LocalDateTime now);
}