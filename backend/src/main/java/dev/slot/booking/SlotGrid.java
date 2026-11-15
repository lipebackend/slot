package dev.slot.booking;

import dev.slot.availability.domain.TimeRange;
import dev.slot.shared.Scheduling;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SlotGrid {
    private SlotGrid() {
    }

    public static List<Slot> generate(LocalDate date, List<TimeRange> intervals, List<Busy> busy, Instant now) {
        LocalDate today = LocalDate.ofInstant(now, Scheduling.ZONE);
        if (date.isBefore(today)) {
            return List.of();
        }
        List<TimeRange> ordered = intervals.stream()
                .sorted(Comparator.comparing(TimeRange::startTime))
                .toList();
        List<Slot> slots = new ArrayList<>();
        for (TimeRange interval : ordered) {
            int startMinute = minuteOf(interval.startTime());
            int endMinute = minuteOf(interval.endTime());
            for (int minute = startMinute; minute + Scheduling.DURATION_MINUTES <= endMinute; minute += Scheduling.DURATION_MINUTES) {
                LocalTime startTime = LocalTime.of(minute / 60, minute % 60);
                ZonedDateTime start = ZonedDateTime.of(date, startTime, Scheduling.ZONE);
                ZonedDateTime end = start.plusMinutes(Scheduling.DURATION_MINUTES);
                if (!start.toInstant().isAfter(now) || overlaps(start.toInstant(), end.toInstant(), busy)) {
                    continue;
                }
                slots.add(new Slot(start.toOffsetDateTime(), end.toOffsetDateTime()));
            }
        }
        return List.copyOf(slots);
    }

    private static boolean overlaps(Instant start, Instant end, List<Busy> busy) {
        for (Busy item : busy) {
            if (start.isBefore(item.endsAt()) && item.startsAt().isBefore(end)) {
                return true;
            }
        }
        return false;
    }

    private static int minuteOf(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    public record Slot(OffsetDateTime startsAt, OffsetDateTime endsAt) {
    }

    public record Busy(Instant startsAt, Instant endsAt) {
    }
}
