package dev.slot.organizer;

import dev.slot.shared.Scheduling;

import java.util.UUID;

public record OrganizerResponse(UUID id, String name, String email, String bookingPath) {
    public static OrganizerResponse of(UUID id, String name, String email) {
        return new OrganizerResponse(id, name, email, Scheduling.bookingPath(id));
    }
}
