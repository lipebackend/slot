package dev.slot.booking;

import java.util.UUID;

public record PublicOrganizerResponse(UUID id, String name, String timezone, int eventDurationMinutes) {
}
