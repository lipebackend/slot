package dev.slot.auth;

import dev.slot.organizer.OrganizerRepository;
import dev.slot.organizer.OrganizerResponse;
import dev.slot.shared.error.ApiException;
import dev.slot.shared.security.OrganizerPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final OrganizerRepository organizers;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public AuthService(OrganizerRepository organizers, PasswordEncoder passwordEncoder) {
        this.organizers = organizers;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("dummy-password-not-a-credential");
    }

    public LoginResult login(LoginRequest request) {
        String email = request == null || request.email() == null
                ? ""
                : request.email().trim().toLowerCase(Locale.ROOT);
        String password = request == null ? null : request.password();
        Optional<OrganizerRepository.Account> account = email.isEmpty()
                ? Optional.empty()
                : organizers.findAccountByEmail(email);
        String hash = account.map(found -> found.passwordHash()).orElse(dummyHash);
        boolean matches = passwordEncoder.matches(password == null ? "" : password, hash);
        if (account.isEmpty() || password == null || !matches) {
            throw ApiException.invalidCredentials();
        }
        OrganizerRepository.Account found = account.get();
        return new LoginResult(
                OrganizerResponse.of(found.id(), found.name(), found.email()),
                new OrganizerPrincipal(found.id(), found.email(), found.name()));
    }

    public OrganizerResponse current(UUID id) {
        return organizers.findProfile(id)
                .map(profile -> OrganizerResponse.of(profile.id(), profile.name(), profile.email()))
                .orElseThrow(ApiException::unauthenticated);
    }

    public record LoginResult(OrganizerResponse response, OrganizerPrincipal principal) {
    }
}
