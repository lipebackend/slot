package dev.slot.availability;

import dev.slot.availability.domain.TimeRange;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.List;

public record DayAvailability(
        @NotNull(message = "Informe o dia da semana.") DayOfWeek dayOfWeek,
        @NotNull(message = "Informe a lista de intervalos.") List<TimeRange> intervals) {
}
