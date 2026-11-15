package dev.slot.booking;

import dev.slot.shared.Scheduling;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookingRepository {
    private final JdbcClient jdbc;

    public BookingRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(UUID id, UUID organizerId, String name, String email, Instant startsAt, Instant endsAt) {
        jdbc.sql("""
                INSERT INTO booking (id, organizer_id, visitor_name, visitor_email, starts_at, ends_at, status)
                VALUES (:id, :organizerId, :name, :email, :startsAt, :endsAt, 'ACTIVE')
                """)
                .param("id", id)
                .param("organizerId", organizerId)
                .param("name", name)
                .param("email", email)
                .param("startsAt", OffsetDateTime.ofInstant(startsAt, Scheduling.ZONE))
                .param("endsAt", OffsetDateTime.ofInstant(endsAt, Scheduling.ZONE))
                .update();
    }

    public List<SlotGrid.Busy> findActiveOverlapping(UUID organizerId, LocalDate date) {
        OffsetDateTime dayStart = date.atStartOfDay(Scheduling.ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(Scheduling.ZONE).toOffsetDateTime();
        return jdbc.sql("""
                SELECT starts_at, ends_at
                FROM booking
                WHERE organizer_id = :organizerId
                  AND status = 'ACTIVE'
                  AND starts_at < :dayEnd
                  AND ends_at > :dayStart
                """)
                .param("organizerId", organizerId)
                .param("dayStart", dayStart)
                .param("dayEnd", dayEnd)
                .query((rs, row) -> new SlotGrid.Busy(readInstant(rs, "starts_at"), readInstant(rs, "ends_at")))
                .list();
    }

    public List<StoredBooking> listByOrganizer(UUID organizerId) {
        return jdbc.sql("""
                SELECT id, visitor_name, visitor_email, starts_at, ends_at, status
                FROM booking
                WHERE organizer_id = :organizerId
                ORDER BY starts_at ASC, id::text ASC
                """)
                .param("organizerId", organizerId)
                .query((rs, row) -> new StoredBooking(
                        rs.getObject("id", UUID.class),
                        rs.getString("visitor_name"),
                        rs.getString("visitor_email"),
                        readInstant(rs, "starts_at"),
                        readInstant(rs, "ends_at"),
                        BookingStatus.valueOf(rs.getString("status"))))
                .list();
    }

    public Optional<StoredBooking> findOwned(UUID organizerId, UUID bookingId) {
        return jdbc.sql("""
                SELECT id, visitor_name, visitor_email, starts_at, ends_at, status
                FROM booking
                WHERE id = :id AND organizer_id = :organizerId
                """)
                .param("id", bookingId)
                .param("organizerId", organizerId)
                .query((rs, row) -> new StoredBooking(
                        rs.getObject("id", UUID.class),
                        rs.getString("visitor_name"),
                        rs.getString("visitor_email"),
                        readInstant(rs, "starts_at"),
                        readInstant(rs, "ends_at"),
                        BookingStatus.valueOf(rs.getString("status"))))
                .optional();
    }

    public void cancel(UUID bookingId) {
        jdbc.sql("UPDATE booking SET status = 'CANCELLED' WHERE id = :id")
                .param("id", bookingId)
                .update();
    }

    private static Instant readInstant(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value.toInstant();
    }

    public record StoredBooking(
            UUID id,
            String name,
            String email,
            Instant startsAt,
            Instant endsAt,
            BookingStatus status) {
    }
}
