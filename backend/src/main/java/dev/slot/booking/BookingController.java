package dev.slot.booking;

import dev.slot.shared.security.CurrentOrganizer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @GetMapping
    public BookingListResponse list() {
        return service.list(CurrentOrganizer.id());
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String bookingId) {
        service.cancel(CurrentOrganizer.id(), bookingId);
        return ResponseEntity.noContent().build();
    }
}
