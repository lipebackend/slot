package dev.slot.booking;

import dev.slot.availability.AvailabilityRepository;
import dev.slot.availability.domain.TimeRange;
import dev.slot.organizer.OrganizerRepository;
import dev.slot.shared.Scheduling;
import dev.slot.shared.error.ApiException;
import dev.slot.shared.error.DatabaseErrors;
import dev.slot.shared.web.PublicIds;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private final OrganizerRepository organizers;
    private final AvailabilityRepository availability;
    private final BookingRepository bookings;
    private final Clock clock;

    public BookingService(OrganizerRepository organizers,
                          AvailabilityRepository availability,
                          BookingRepository bookings,
                          Clock clock) {
        this.organizers = organizers;
        this.availability = availability;
        this.bookings = bookings;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PublicOrganizerResponse profile(String rawOrganizerId) {
        UUID organizerId = PublicIds.parse(rawOrganizerId, "organizerId");
        OrganizerRepository.Profile profile = organizers.findProfile(organizerId).orElseThrow(ApiException::notFound);
        return new PublicOrganizerResponse(profile.id(), profile.name(), Scheduling.ZONE_ID, Scheduling.DURATION_MINUTES);
    }

    @Transactional(readOnly = true)
    public SlotsResponse slots(String rawOrganizerId, String rawDate) {
        UUID organizerId = PublicIds.parse(rawOrganizerId, "organizerId");
        LocalDate date = parseDate(rawDate);
        if (organizers.findProfile(organizerId).isEmpty()) {
            throw ApiException.notFound();
        }
        return new SlotsResponse(date, Scheduling.ZONE_ID, toResponses(openSlots(organizerId, date)));
    }

    @Transactional
    public BookingCreatedResponse create(String rawOrganizerId, CreateBookingRequest request) {
        UUID organizerId = PublicIds.parse(rawOrganizerId, "organizerId");
        BookingRules.NormalizedBooking normalized = BookingRules.normalize(request);
        if (!organizers.lock(organizerId)) {
            throw ApiException.notFound();
        }
        OrganizerRepository.Profile profile = organizers.findProfile(organizerId).orElseThrow(ApiException::notFound);
        Instant startsAt = normalized.startsAt().toInstant();
        ZonedDateTime localStart = startsAt.atZone(Scheduling.ZONE);
        SlotGrid.Slot slot = openSlots(organizerId, localStart.toLocalDate()).stream()
                .filter(candidate -> candidate.startsAt().toInstant().equals(startsAt))
                .findFirst()
                .orElseThrow(ApiException::slotUnavailable);
        UUID bookingId = UUID.randomUUID();
        try {
            bookings.insert(bookingId, organizerId, normalized.name(), normalized.email(), startsAt, slot.endsAt().toInstant());
        } catch (DataIntegrityViolationException exception) {
            if (DatabaseErrors.isActiveBookingOverlap(exception)) {
                throw ApiException.slotUnavailable();
            }
            throw exception;
        }
        return new BookingCreatedResponse(
                bookingId,
                profile.name(),
                slot.startsAt(),
                slot.endsAt(),
                Scheduling.ZONE_ID,
                BookingStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public BookingListResponse list(UUID organizerId) {
        List<BookingListResponse.BookingItemResponse> items = bookings.listByOrganizer(organizerId).stream()
                .map(this::toItem)
                .toList();
        return new BookingListResponse(items);
    }

    @Transactional
    public void cancel(UUID organizerId, String rawBookingId) {
        UUID bookingId = PublicIds.parse(rawBookingId, "bookingId");
        BookingRepository.StoredBooking booking = bookings.findOwned(organizerId, bookingId)
                .orElseThrow(ApiException::notFound);
        if (booking.status() == BookingStatus.CANCELLED) {
            return;
        }
        bookings.cancel(bookingId);
    }

    private List<SlotGrid.Slot> openSlots(UUID organizerId, LocalDate date) {
        List<TimeRange> intervals = availability.findIntervals(organizerId, date.getDayOfWeek());
        List<SlotGrid.Busy> busy = bookings.findActiveOverlapping(organizerId, date);
        return SlotGrid.generate(date, intervals, busy, clock.instant());
    }

    private BookingListResponse.BookingItemResponse toItem(BookingRepository.StoredBooking booking) {
        return new BookingListResponse.BookingItemResponse(
                booking.id(),
                booking.name(),
                booking.email(),
                toOffset(booking.startsAt()),
                toOffset(booking.endsAt()),
                booking.status());
    }

    private static List<SlotsResponse.SlotResponse> toResponses(List<SlotGrid.Slot> slots) {
        return slots.stream()
                .map(slot -> new SlotsResponse.SlotResponse(slot.startsAt(), slot.endsAt()))
                .toList();
    }

    private static OffsetDateTime toOffset(Instant instant) {
        return instant.atZone(Scheduling.ZONE).toOffsetDateTime();
    }

    private static LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            throw ApiException.validation("date", "Informe a data no formato YYYY-MM-DD.");
        }
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeException ex) {
            throw ApiException.validation("date", "Informe a data no formato YYYY-MM-DD.");
        }
    }
}
