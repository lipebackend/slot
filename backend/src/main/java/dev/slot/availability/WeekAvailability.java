package dev.slot.availability;

import java.util.List;

public record WeekAvailability(String timezone, List<DayAvailability> days) {
}
