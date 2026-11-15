package dev.slot.booking;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record SlotsResponse(LocalDate date, String timezone, List<SlotResponse> slots) {
    public record SlotResponse(
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime startsAt,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime endsAt) {
    }
}
