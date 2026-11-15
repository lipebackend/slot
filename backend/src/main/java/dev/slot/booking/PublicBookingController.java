package dev.slot.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/organizers/{organizerId}")
public class PublicBookingController {
    private final BookingService service;

    public PublicBookingController(BookingService service) {
        this.service = service;
    }

    @GetMapping
    public PublicOrganizerResponse profile(@PathVariable String organizerId) {
        return service.profile(organizerId);
    }

    @GetMapping("/slots")
    public SlotsResponse slots(@PathVariable String organizerId,
                               @RequestParam(name = "date", required = false) String date) {
        return service.slots(organizerId, date);
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingCreatedResponse> create(@PathVariable String organizerId,
                                                         @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(organizerId, request));
    }
}
