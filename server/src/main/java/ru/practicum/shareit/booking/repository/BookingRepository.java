package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentByBookerId(Long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentByOwnerId(Long ownerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.end < :now")
    List<Booking> findPastByBookerId(Long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < :now")
    List<Booking> findPastByOwnerId(Long ownerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.start > :now")
    List<Booking> findFutureByBookerId(Long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > :now")
    List<Booking> findFutureByOwnerId(Long ownerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start < :now AND b.status = :status ORDER BY b.start DESC LIMIT 1")
    Optional<Booking> findLastBookingByItemIdAndStatusBeforeNow(Long itemId, LocalDateTime now, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start > :now AND b.status = :status ORDER BY b.start ASC LIMIT 1")
    Optional<Booking> findNextBookingByItemIdAndStatusAfterNow(Long itemId, LocalDateTime now, BookingStatus status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean hasApprovedPastBooking(@Param("userId") Long userId,
                                   @Param("itemId") Long itemId,
                                   @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.item.id = :itemId " +
            "AND b.end < :end " +
            "AND b.status = :status")
    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(@Param("bookerId") Long bookerId,
                                                           @Param("itemId") Long itemId,
                                                           @Param("end") LocalDateTime end,
                                                           @Param("status") BookingStatus status);


    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND (:state = 'ALL' OR " +
            "(:state = 'PAST' AND b.end < :now) OR " +
            "(:state = 'FUTURE' AND b.start > :now) OR " +
            "(:state = 'CURRENT' AND b.start <= :now AND b.end >= :now) OR " +
            "(:state = 'WAITING' AND b.status = 'WAITING') OR " +
            "(:state = 'REJECTED' AND b.status = 'REJECTED')) " +
            "ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndState(@Param("bookerId") Long bookerId,
                                         @Param("state") String state,  // ← String, а не BookingState!
                                         @Param("now") LocalDateTime now,
                                         Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId " +
            "AND (:state = 'ALL' OR " +
            "(:state = 'PAST' AND b.end < :now) OR " +
            "(:state = 'FUTURE' AND b.start > :now) OR " +
            "(:state = 'CURRENT' AND b.start <= :now AND b.end >= :now) OR " +
            "(:state = 'WAITING' AND b.status = 'WAITING') OR " +
            "(:state = 'REJECTED' AND b.status = 'REJECTED')) " +
            "ORDER BY b.start DESC")
    List<Booking> findByOwnerIdAndState(@Param("ownerId") Long ownerId,
                                        @Param("state") String state,  // ← String, а не BookingState!
                                        @Param("now") LocalDateTime now,
                                        Pageable pageable);

    default List<Booking> findByBookerIdAndState(Long bookerId, BookingState state, LocalDateTime now) {
        return switch (state) {
            case ALL -> findByBookerIdOrderByStartDesc(bookerId);
            case CURRENT -> findCurrentByBookerId(bookerId, now);
            case PAST -> findPastByBookerId(bookerId, now);
            case FUTURE -> findFutureByBookerId(bookerId, now);
            case WAITING -> findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
            case REJECTED -> findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
        };
    }

    default List<Booking> findByOwnerIdAndState(Long ownerId, BookingState state, LocalDateTime now) {
        return switch (state) {
            case ALL -> findByItemOwnerIdOrderByStartDesc(ownerId);
            case CURRENT -> findCurrentByOwnerId(ownerId, now);
            case PAST -> findPastByOwnerId(ownerId, now);
            case FUTURE -> findFutureByOwnerId(ownerId, now);
            case WAITING -> findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case REJECTED -> findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
        };
    }
}