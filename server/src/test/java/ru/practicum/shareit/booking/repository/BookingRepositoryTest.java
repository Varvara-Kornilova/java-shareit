package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

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
    void setUp() {
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
    void hasApprovedPastBooking_ReturnsTrue_WhenApprovedBookingExists() {
        Booking booking = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean result = bookingRepository.hasApprovedPastBooking(
                booker.getId(), item.getId(), now);

        assertTrue(result, "Должно вернуть true для одобренного прошедшего бронирования");
    }

    @Test
    void hasApprovedPastBooking_ReturnsFalse_WhenStatusIsNotApproved() {
        Booking booking = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.WAITING);
        bookingRepository.save(booking);

        boolean result = bookingRepository.hasApprovedPastBooking(
                booker.getId(), item.getId(), now);

        assertFalse(result, "Должно вернуть false, если статус не APPROVED");
    }

    @Test
    void hasApprovedPastBooking_ReturnsFalse_WhenBookingIsFuture() {
        Booking booking = createBooking(booker, item,
                now.plusDays(5), now.plusDays(10), BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean result = bookingRepository.hasApprovedPastBooking(
                booker.getId(), item.getId(), now);

        assertFalse(result, "Должно вернуть false для будущего бронирования");
    }


    @Test
    void findNextBookingByItemIdAndStatusAfterNow_ReturnsNearestFutureBooking() {
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
    void findNextBookingByItemIdAndStatusAfterNow_ReturnsEmpty_WhenNoFutureBookings() {
        Booking past = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(past);

        Optional<Booking> result = bookingRepository.findNextBookingByItemIdAndStatusAfterNow(
                item.getId(), now, BookingStatus.APPROVED);

        assertTrue(result.isEmpty());
    }


    @Test
    void findLastBookingByItemIdAndStatusBeforeNow_ReturnsNearestPastBooking() {
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
    void findByBookerIdAndState_Future_ReturnsOnlyFutureBookings() {
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
    void findByBookerIdAndState_Past_ReturnsOnlyPastBookings() {
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
    void findByBookerIdAndState_Current_ReturnsOnlyCurrentBookings() {
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
    void findByBookerIdAndState_All_ReturnsAllBookingsSortedByStartDesc() {
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
    void findByBookerIdAndState_Waiting_ReturnsOnlyWaitingBookings() {
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
    void findByOwnerIdAndState_Future_ReturnsBookingsForOwnerItems() {
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
    void existsByBookerIdAndItemIdAndEndBeforeAndStatus_ReturnsTrue_WhenExists() {
        Booking booking = createBooking(booker, item,
                now.minusDays(10), now.minusDays(5), BookingStatus.APPROVED);
        bookingRepository.save(booking);

        boolean exists = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                booker.getId(), item.getId(), now, BookingStatus.APPROVED);

        assertTrue(exists);
    }

    @Test
    void existsByBookerIdAndItemIdAndEndBeforeAndStatus_ReturnsFalse_WhenNotExists() {
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
}