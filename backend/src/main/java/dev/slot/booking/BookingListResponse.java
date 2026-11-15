package dev.slot.booking;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BookingListResponse(List<BookingItemResponse> bookings) {
    public record BookingItemResponse(
            UUID id,
            String name,
            String email,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime startsAt,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime endsAt,
            BookingStatus status) {
    }
}
