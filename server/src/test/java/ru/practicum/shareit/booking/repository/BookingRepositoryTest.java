package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User booker;
    private User owner;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        now = LocalDateTime.now();

        booker = new User();
        booker.setEmail("booker@test.com");
        booker.setName("Booker");
        booker = userRepository.save(booker);

        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setName("Owner");
        owner = userRepository.save(owner);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }


    @Test
    public void hasApprovedPastBooking_ReturnsTrue_WhenApprovedBookingExists() {
        Booking booking = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean result = bookingRepository.hasApprovedPastBooking(
                booker.getId(), item.getId(), now);

        assertTrue(result, "Должно вернуть true для одобренного прошедшего бронирования");
    }

    @Test
    public void hasApprovedPastBooking_ReturnsFalse_WhenStatusIsNotApproved() {
        Booking booking = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.WAITING);
        bookingRepository.save(booking);

        boolean result = bookingRepository.hasApprovedPastBooking(
                booker.getId(), item.getId(), now);

        assertFalse(result, "Должно вернуть false, если статус не APPROVED");
    }

    @Test
    public void hasApprovedPastBooking_ReturnsFalse_WhenBookingIsFuture() {
        Booking booking = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean result = bookingRepository.hasApprovedPastBooking(
                booker.getId(), item.getId(), now);

        assertFalse(result, "Должно вернуть false для будущего бронирования");
    }


    @Test
    public void findNextBookingByItemIdAndStatusAfterNow_ReturnsNearestFutureBooking() {
        Booking first = createBooking(booker, item,
                now.plusDays(5), now.plusDays(7), BookingStatus.APPROVED);
        Booking second = createBooking(booker, item,
                now.plusDays(10), now.plusDays(12), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(first, second));

        Optional<Booking> result = bookingRepository.findNextBookingByItemIdAndStatusAfterNow(
                item.getId(), now, BookingStatus.APPROVED);

        assertTrue(result.isPresent());
        assertEquals(first.getId(), result.get().getId());
    }

    @Test
    public void findNextBookingByItemIdAndStatusAfterNow_ReturnsEmpty_WhenNoFutureBookings() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(past);

        Optional<Booking> result = bookingRepository.findNextBookingByItemIdAndStatusAfterNow(
                item.getId(), now, BookingStatus.APPROVED);

        assertTrue(result.isEmpty());
    }


    @Test
    public void findLastBookingByItemIdAndStatusBeforeNow_ReturnsNearestPastBooking() {
        Booking first = createBooking(booker, item,
                now.minusDays(15), now.minusDays(10), BookingStatus.APPROVED);
        Booking second = createBooking(booker, item,
                now.minusDays(5), now.minusDays(2), BookingStatus.APPROVED); // ближе к now
        bookingRepository.saveAll(List.of(first, second));

        Optional<Booking> result = bookingRepository.findLastBookingByItemIdAndStatusBeforeNow(
                item.getId(), now, BookingStatus.APPROVED);

        assertTrue(result.isPresent());
        assertEquals(second.getId(), result.get().getId());
    }


    @Test
    public void findByBookerIdAndState_Future_ReturnsOnlyFutureBookings() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        Booking future = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(past, future));

        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), BookingState.FUTURE, now);

        assertEquals(1, result.size());
        assertEquals(future.getId(), result.get(0).getId());
        assertTrue(result.get(0).getStart().isAfter(now));
    }

    @Test
    public void findByBookerIdAndState_Past_ReturnsOnlyPastBookings() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        Booking future = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(past, future));

        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), BookingState.PAST, now);

        assertEquals(1, result.size());
        assertEquals(past.getId(), result.get(0).getId());
        assertTrue(result.get(0).getEnd().isBefore(now));
    }

    @Test
    public void findByBookerIdAndState_Current_ReturnsOnlyCurrentBookings() {
        Booking current = createBooking(booker, item,
                now.minusDays(2), now.plusDays(2), BookingStatus.APPROVED);
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(current, past));

        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), BookingState.CURRENT, now);

        assertEquals(1, result.size());
        assertEquals(current.getId(), result.get(0).getId());
    }

    @Test
    public void findByBookerIdAndState_All_ReturnsAllBookingsSortedByStartDesc() {
        Booking first = createBooking(booker, item,
                now.minusDays(5), now.minusDays(2), BookingStatus.APPROVED);
        Booking second = createBooking(booker, item,
                now.plusDays(10), now.plusDays(15), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(first, second));

        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), BookingState.ALL, now);

        assertEquals(2, result.size());
        assertEquals(second.getId(), result.get(0).getId());
        assertEquals(first.getId(), result.get(1).getId());
    }

    @Test
    public void findByBookerIdAndState_Waiting_ReturnsOnlyWaitingBookings() {
        Booking waiting = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.WAITING);
        Booking approved = createBooking(booker, item,
                now.plusDays(15), now.plusDays(20), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(waiting, approved));

        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), BookingState.WAITING, now);

        assertEquals(1, result.size());
        assertEquals(waiting.getId(), result.get(0).getId());
        assertEquals(BookingStatus.WAITING, result.get(0).getStatus());
    }


    @Test
    public void findByOwnerIdAndState_Future_ReturnsBookingsForOwnerItems() {
        Booking booking = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.save(booking);

        List<Booking> result = bookingRepository.findByOwnerIdAndState(
                owner.getId(), BookingState.FUTURE, now);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
        assertEquals(item.getId(), result.get(0).getItem().getId());
    }

    @Test
    public void existsByBookerIdAndItemIdAndEndBeforeAndStatus_ReturnsTrue_WhenExists() {
        Booking booking = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean exists = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                booker.getId(), item.getId(), now, BookingStatus.APPROVED);

        assertTrue(exists);
    }

    @Test
    public void existsByBookerIdAndItemIdAndEndBeforeAndStatus_ReturnsFalse_WhenNotExists() {
        Booking booking = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.WAITING);
        bookingRepository.save(booking);

        boolean exists = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                booker.getId(), item.getId(), now, BookingStatus.APPROVED);

        assertFalse(exists);
    }


    private Booking createBooking(User booker, Item item,
                                  LocalDateTime start, LocalDateTime end,
                                  BookingStatus status) {
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return booking;
    }

    @Test
    public void findByBookerIdAndState_Rejected_ReturnsOnlyRejectedBookings() {
        Booking rejected = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.REJECTED);
        Booking approved = createBooking(booker, item,
                now.plusDays(15), now.plusDays(20), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(rejected, approved));

        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), BookingState.REJECTED, now);

        assertEquals(1, result.size());
        assertEquals(rejected.getId(), result.get(0).getId());
        assertEquals(BookingStatus.REJECTED, result.get(0).getStatus());
    }

    @Test
    public void findByBookerIdAndState_All_ReturnsEmpty_WhenNoBookings() {
        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), BookingState.ALL, now);
        assertTrue(result.isEmpty());
    }

    @Test
    public void findByBookerIdAndState_Past_ReturnsEmpty_WhenNoPastBookings() {
        Booking future = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.save(future);

        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), BookingState.PAST, now);
        assertTrue(result.isEmpty());
    }

    @Test
    public void findByOwnerIdAndState_Past_ReturnsBookingsForOwnerItems() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(past);

        List<Booking> result = bookingRepository.findByOwnerIdAndState(
                owner.getId(), BookingState.PAST, now);

        assertEquals(1, result.size());
        assertEquals(past.getId(), result.get(0).getId());
    }

    @Test
    public void findByOwnerIdAndState_Current_ReturnsBookingsForOwnerItems() {
        Booking current = createBooking(booker, item,
                now.minusDays(2), now.plusDays(2), BookingStatus.APPROVED);
        bookingRepository.save(current);

        List<Booking> result = bookingRepository.findByOwnerIdAndState(
                owner.getId(), BookingState.CURRENT, now);

        assertEquals(1, result.size());
        assertEquals(current.getId(), result.get(0).getId());
    }

    @Test
    public void findByOwnerIdAndState_All_ReturnsAllBookingsForOwner() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        Booking future = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(past, future));

        List<Booking> result = bookingRepository.findByOwnerIdAndState(
                owner.getId(), BookingState.ALL, now);

        assertEquals(2, result.size());
    }

    @Test
    public void findByOwnerIdAndState_Waiting_ReturnsOnlyWaitingBookings() {
        Booking waiting = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.WAITING);
        Booking approved = createBooking(booker, item,
                now.plusDays(15), now.plusDays(20), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(waiting, approved));

        List<Booking> result = bookingRepository.findByOwnerIdAndState(
                owner.getId(), BookingState.WAITING, now);

        assertEquals(1, result.size());
        assertEquals(waiting.getId(), result.get(0).getId());
    }

    @Test
    public void findByOwnerIdAndState_Rejected_ReturnsOnlyRejectedBookings() {
        Booking rejected = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.REJECTED);
        bookingRepository.save(rejected);

        List<Booking> result = bookingRepository.findByOwnerIdAndState(
                owner.getId(), BookingState.REJECTED, now);

        assertEquals(1, result.size());
        assertEquals(rejected.getId(), result.get(0).getId());
    }

    @Test
    public void findByOwnerIdAndState_All_ReturnsEmpty_WhenNoBookingsForOwner() {
        // Создаём вещь другого владельца
        User anotherOwner = new User();
        anotherOwner.setEmail("another@test.com");
        anotherOwner.setName("Another");
        anotherOwner = userRepository.save(anotherOwner);

        Item anotherItem = new Item();
        anotherItem.setName("Another Item");
        anotherItem.setDescription("Another");
        anotherItem.setAvailable(true);
        anotherItem.setOwner(anotherOwner);
        anotherItem = itemRepository.save(anotherItem);

        Booking booking = createBooking(booker, anotherItem,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.save(booking);

        List<Booking> result = bookingRepository.findByOwnerIdAndState(
                owner.getId(), BookingState.ALL, now);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findByBookerIdAndState_WithPageable_ReturnsLimitedResults() {
        for (int i = 0; i < 5; i++) {
            Booking booking = createBooking(booker, item,
                    now.plusDays(i), now.plusDays(i + 2), BookingStatus.APPROVED);
            bookingRepository.save(booking);
        }

        List<Booking> result = bookingRepository.findByBookerIdAndState(
                booker.getId(), "ALL", now,
                org.springframework.data.domain.PageRequest.of(0, 2));

        assertEquals(2, result.size());
    }

    @Test
    public void findByOwnerIdAndState_WithPageable_ReturnsLimitedResults() {
        for (int i = 0; i < 5; i++) {
            Booking booking = createBooking(booker, item,
                    now.plusDays(i), now.plusDays(i + 2), BookingStatus.APPROVED);
            bookingRepository.save(booking);
        }

        List<Booking> result = bookingRepository.findByOwnerIdAndState(
                owner.getId(), "ALL", now,
                org.springframework.data.domain.PageRequest.of(0, 3));

        assertEquals(3, result.size());
    }

    @Test
    public void hasApprovedPastBooking_ReturnsFalse_WhenNoBookingsAtAll() {
        boolean result = bookingRepository.hasApprovedPastBooking(
                999L, 999L, now);
        assertFalse(result);
    }

    @Test
    public void hasApprovedPastBooking_ReturnsFalse_WhenBookingEndsExactlyAtNow() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 6, 1, 12, 0, 0);

        Booking booking = createBooking(booker, item,
                fixedNow.minusDays(10), fixedNow, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean result = bookingRepository.hasApprovedPastBooking(
                booker.getId(), item.getId(), fixedNow);

        assertFalse(result, "Бронирование с end == now не считается прошедшим");
    }

    @Test
    public void existsByBookerIdAndItemIdAndEndBeforeAndStatus_ReturnsFalse_WhenEndEqualsParameter() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 6, 1, 12, 0, 0);

        Booking booking = createBooking(booker, item,
                fixedNow.minusDays(10), fixedNow, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean exists = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                booker.getId(), item.getId(), fixedNow, BookingStatus.APPROVED);

        assertFalse(exists, "Бронирование с end == параметру не удовлетворяет условию end < параметр");
    }

    @Test
    public void existsByBookerIdAndItemIdAndEndBeforeAndStatus_ReturnsFalse_WhenNoBookingsAtAll() {
        boolean exists = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                999L, 999L, now, BookingStatus.APPROVED);
        assertFalse(exists);
    }

    @Test
    public void findCurrentByBookerId_ReturnsCurrentBookings() {
        Booking current = createBooking(booker, item,
                now.minusDays(2), now.plusDays(2), BookingStatus.APPROVED);
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(current, past));

        List<Booking> result = bookingRepository.findCurrentByBookerId(booker.getId(), now);

        assertEquals(1, result.size());
        assertEquals(current.getId(), result.get(0).getId());
    }

    @Test
    public void findCurrentByOwnerId_ReturnsCurrentBookings() {
        Booking current = createBooking(booker, item,
                now.minusDays(2), now.plusDays(2), BookingStatus.APPROVED);
        bookingRepository.save(current);

        List<Booking> result = bookingRepository.findCurrentByOwnerId(owner.getId(), now);

        assertEquals(1, result.size());
        assertEquals(current.getId(), result.get(0).getId());
    }

    @Test
    public void findCurrentByBookerId_ReturnsEmpty_WhenNoCurrentBookings() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        Booking future = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.saveAll(List.of(past, future));

        List<Booking> result = bookingRepository.findCurrentByBookerId(booker.getId(), now);

        assertTrue(result.isEmpty());
    }


    @Test
    public void findPastByBookerId_ReturnsPastBookings() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(past);

        List<Booking> result = bookingRepository.findPastByBookerId(booker.getId(), now);

        assertEquals(1, result.size());
        assertEquals(past.getId(), result.get(0).getId());
    }

    @Test
    public void findPastByOwnerId_ReturnsPastBookings() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(past);

        List<Booking> result = bookingRepository.findPastByOwnerId(owner.getId(), now);

        assertEquals(1, result.size());
        assertEquals(past.getId(), result.get(0).getId());
    }


    @Test
    public void findFutureByBookerId_ReturnsFutureBookings() {
        Booking future = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.save(future);

        List<Booking> result = bookingRepository.findFutureByBookerId(booker.getId(), now);

        assertEquals(1, result.size());
        assertEquals(future.getId(), result.get(0).getId());
    }

    @Test
    public void findFutureByOwnerId_ReturnsFutureBookings() {
        Booking future = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.save(future);

        List<Booking> result = bookingRepository.findFutureByOwnerId(owner.getId(), now);

        assertEquals(1, result.size());
        assertEquals(future.getId(), result.get(0).getId());
    }
}