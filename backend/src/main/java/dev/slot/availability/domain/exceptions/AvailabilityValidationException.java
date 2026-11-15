package dev.slot.availability.domain.exceptions;

public class AvailabilityValidationException extends IllegalArgumentException {
    public AvailabilityValidationException(String message) {
        super(message);
    }
}
