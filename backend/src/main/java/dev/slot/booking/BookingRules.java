package dev.slot.booking;

import dev.slot.shared.error.ApiException;
import dev.slot.shared.error.FieldErrorDetail;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class BookingRules {
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    private BookingRules() {
    }

    static NormalizedBooking normalize(CreateBookingRequest request) {
        if (request == null) {
            throw ApiException.validation(List.of());
        }
        List<FieldErrorDetail> errors = new ArrayList<>();
        String name = normalizeName(request.name(), errors);
        String email = normalizeEmail(request.email(), errors);
        OffsetDateTime startsAt = parseStart(request.startsAt(), errors);
        if (!errors.isEmpty()) {
            throw ApiException.validation(errors);
        }
        return new NormalizedBooking(name, email, startsAt);
    }

    private static String normalizeName(String raw, List<FieldErrorDetail> errors) {
        if (raw == null) {
            errors.add(new FieldErrorDetail("name", "Informe o nome."));
            return null;
        }
        String name = raw.trim();
        if (name.isEmpty()) {
            errors.add(new FieldErrorDetail("name", "Informe o nome."));
            return null;
        }
        if (name.length() > 120) {
            errors.add(new FieldErrorDetail("name", "O nome deve ter no máximo 120 caracteres."));
            return null;
        }
        return name;
    }

    private static String normalizeEmail(String raw, List<FieldErrorDetail> errors) {
        if (raw == null) {
            errors.add(new FieldErrorDetail("email", "Informe um e-mail válido."));
            return null;
        }
        String email = raw.trim();
        if (email.isEmpty() || email.length() > 254 || !EMAIL.matcher(email).matches()) {
            errors.add(new FieldErrorDetail("email", "Informe um e-mail válido."));
            return null;
        }
        return email;
    }

    private static OffsetDateTime parseStart(String raw, List<FieldErrorDetail> errors) {
        if (raw == null || raw.isBlank()) {
            errors.add(new FieldErrorDetail("startsAt", "Informe um instante ISO 8601 com offset."));
            return null;
        }
        try {
            return OffsetDateTime.parse(raw);
        } catch (DateTimeException ex) {
            errors.add(new FieldErrorDetail("startsAt", "Informe um instante ISO 8601 com offset."));
            return null;
        }
    }

    record NormalizedBooking(String name, String email, OffsetDateTime startsAt) {
    }
}
