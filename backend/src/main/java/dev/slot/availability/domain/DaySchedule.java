package dev.slot.availability.domain;

import dev.slot.availability.domain.exceptions.AvailabilityValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class DaySchedule {
    private DaySchedule() {}

    public static List<TimeRange> validate(List<TimeRange> intervals) {
        if (intervals == null || intervals.stream().anyMatch(Objects::isNull)) {
            throw new AvailabilityValidationException("Informe a lista de intervalos.");
        }
        var sorted = intervals.stream()
                .sorted(Comparator.comparing(TimeRange::startTime)).toList();
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).startTime().isBefore(sorted.get(i - 1).endTime())) {
                throw new AvailabilityValidationException("Intervalos do mesmo dia não podem se sobrepor.");
            }
        }
        return sorted;
    }

}
