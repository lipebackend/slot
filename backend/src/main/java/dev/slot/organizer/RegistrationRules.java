package dev.slot.organizer;

import dev.slot.shared.error.ApiException;
import dev.slot.shared.error.FieldErrorDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class RegistrationRules {
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    private RegistrationRules() {
    }

    static NormalizedOrganizer normalize(RegisterOrganizerRequest request) {
        if (request == null) {
            throw ApiException.validation(List.of());
        }
        List<FieldErrorDetail> errors = new ArrayList<>();
        String name = normalizeName(request.name(), errors);
        String email = normalizeEmail(request.email(), errors);
        String password = request.password();
        if (password == null || password.length() < 8 || password.length() > 128) {
            errors.add(new FieldErrorDetail("password", "A senha deve ter entre 8 e 128 caracteres."));
        }
        if (!errors.isEmpty()) {
            throw ApiException.validation(errors);
        }
        return new NormalizedOrganizer(name, email, password);
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
        String email = raw.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty() || email.length() > 254 || !EMAIL.matcher(email).matches()) {
            errors.add(new FieldErrorDetail("email", "Informe um e-mail válido."));
            return null;
        }
        return email;
    }

    record NormalizedOrganizer(String name, String email, String password) {
        @Override
        public String toString() {
            return "NormalizedOrganizer[" + email + "]";
        }
    }
}
