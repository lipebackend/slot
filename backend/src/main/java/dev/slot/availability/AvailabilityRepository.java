package dev.slot.availability;

import dev.slot.availability.domain.TimeRange;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Repository
public class AvailabilityRepository {
    private final JdbcClient jdbc;

    public AvailabilityRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void createDefaultWeek(UUID organizerId) {
        for (DayOfWeek day : DayOfWeek.values()) {
            jdbc.sql("""
                    INSERT INTO availability_day (organizer_id, day_of_week)
                    VALUES (:organizerId, :day)
                    """)
                    .param("organizerId", organizerId)
                    .param("day", day.name())
                    .update();
        }
        for (DayOfWeek day : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)) {
            insertInterval(organizerId, day, LocalTime.of(9, 0), LocalTime.NOON);
        }
    }

    public List<StoredInterval> findAll(UUID organizerId) {
        return jdbc.sql("""
                SELECT day_of_week, start_time, end_time
                FROM availability_interval
                WHERE organizer_id = :organizerId
                ORDER BY day_of_week, start_time
                """)
                .param("organizerId", organizerId)
                .query((rs, row) -> new StoredInterval(
                        DayOfWeek.valueOf(rs.getString("day_of_week")),
                        new TimeRange(
                                rs.getObject("start_time", LocalTime.class),
                                rs.getObject("end_time", LocalTime.class))))
                .list();
    }

    public List<TimeRange> findIntervals(UUID organizerId, DayOfWeek day) {
        return jdbc.sql("""
                SELECT start_time, end_time
                FROM availability_interval
                WHERE organizer_id = :organizerId AND day_of_week = :day
                ORDER BY start_time
                """)
                .param("organizerId", organizerId)
                .param("day", day.name())
                .query((rs, row) -> new TimeRange(
                        rs.getObject("start_time", LocalTime.class),
                        rs.getObject("end_time", LocalTime.class)))
                .list();
    }

    public void replace(UUID organizerId, DayOfWeek day, List<TimeRange> intervals) {
        jdbc.sql("""
                INSERT INTO availability_day (organizer_id, day_of_week)
                VALUES (:organizerId, :day)
                ON CONFLICT DO NOTHING
                """)
                .param("organizerId", organizerId)
                .param("day", day.name())
                .update();
        jdbc.sql("""
                SELECT day_of_week
                FROM availability_day
                WHERE organizer_id = :organizerId AND day_of_week = :day
                FOR UPDATE
                """)
                .param("organizerId", organizerId)
                .param("day", day.name())
                .query(String.class)
                .single();
        jdbc.sql("""
                DELETE FROM availability_interval
                WHERE organizer_id = :organizerId AND day_of_week = :day
                """)
                .param("organizerId", organizerId)
                .param("day", day.name())
                .update();
        for (TimeRange interval : intervals) {
            insertInterval(organizerId, day, interval.startTime(), interval.endTime());
        }
    }

    private void insertInterval(UUID organizerId, DayOfWeek day, LocalTime start, LocalTime end) {
        jdbc.sql("""
                INSERT INTO availability_interval (organizer_id, day_of_week, start_time, end_time)
                VALUES (:organizerId, :day, :start, :end)
                """)
                .param("organizerId", organizerId)
                .param("day", day.name())
                .param("start", start)
                .param("end", end)
                .update();
    }

    public record StoredInterval(DayOfWeek day, TimeRange range) {
    }
}
