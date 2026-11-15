package dev.slot.availability.domain;

import dev.slot.availability.domain.exceptions.AvailabilityValidationException;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record TimeRange(
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime
) {
    public TimeRange {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new AvailabilityValidationException("O início deve ser anterior ao fim.");
        }
        if (hasSeconds(startTime) || hasSeconds(endTime)) {
            throw new AvailabilityValidationException("Horários devem usar precisão de minutos.");
        }
    }

    private static boolean hasSeconds(LocalTime time) {
        return time.getSecond() != 0 || time.getNano() != 0;
    }
}
