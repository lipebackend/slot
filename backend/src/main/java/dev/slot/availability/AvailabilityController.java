package dev.slot.availability;

import dev.slot.shared.security.CurrentOrganizer;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {
    private final AvailabilityService service;

    public AvailabilityController(AvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public WeekAvailability list() {
        return service.list(CurrentOrganizer.id());
    }

    @PostMapping
    public DayAvailability replace(@Valid @RequestBody DayAvailability request) {
        return service.replace(CurrentOrganizer.id(), request.dayOfWeek(), request.intervals());
    }
}
