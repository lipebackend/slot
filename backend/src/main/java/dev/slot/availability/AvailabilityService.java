package dev.slot.availability;

import dev.slot.availability.domain.DaySchedule;
import dev.slot.availability.domain.TimeRange;
import dev.slot.organizer.OrganizerRepository;
import dev.slot.shared.Scheduling;
import dev.slot.shared.error.ApiException;
import dev.slot.shared.error.DatabaseErrors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {
    private final AvailabilityRepository availability;
    private final OrganizerRepository organizers;

    public AvailabilityService(AvailabilityRepository availability, OrganizerRepository organizers) {
        this.availability = availability;
        this.organizers = organizers;
    }

    @Transactional(readOnly = true)
    public WeekAvailability list(UUID organizerId) {
        if (organizers.findProfile(organizerId).isEmpty()) {
            throw ApiException.notFound();
        }
        List<AvailabilityRepository.StoredInterval> stored = availability.findAll(organizerId);
        List<DayAvailability> days = Arrays.stream(DayOfWeek.values())
                .map(day -> new DayAvailability(day, stored.stream()
                        .filter(interval -> interval.day() == day)
                        .map(AvailabilityRepository.StoredInterval::range)
                        .toList()))
                .toList();
        return new WeekAvailability(Scheduling.ZONE_ID, days);
    }

    @Transactional
    public DayAvailability replace(UUID organizerId, DayOfWeek day, List<TimeRange> intervals) {
        List<TimeRange> validated = DaySchedule.validate(intervals);
        // A trava do organizador serializa a troca do dia com a criação de reservas.
        if (!organizers.lock(organizerId)) {
            throw ApiException.notFound();
        }
        try {
            availability.replace(organizerId, day, validated);
        } catch (DataIntegrityViolationException exception) {
            if (DatabaseErrors.isIntervalOverlap(exception)) {
                throw new dev.slot.availability.domain.exceptions.AvailabilityValidationException(
                        "Intervalos do mesmo dia não podem se sobrepor.");
            }
            throw exception;
        }
        return new DayAvailability(day, validated);
    }
}
