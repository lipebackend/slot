package dev.slot.organizer;

import dev.slot.availability.AvailabilityRepository;
import dev.slot.shared.error.ApiException;
import dev.slot.shared.error.DatabaseErrors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizerService {
    private final OrganizerRepository organizers;
    private final AvailabilityRepository availability;
    private final PasswordEncoder passwordEncoder;

    public OrganizerService(OrganizerRepository organizers,
                            AvailabilityRepository availability,
                            PasswordEncoder passwordEncoder) {
        this.organizers = organizers;
        this.availability = availability;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public OrganizerResponse register(RegisterOrganizerRequest request) {
        RegistrationRules.NormalizedOrganizer normalized = RegistrationRules.normalize(request);
        UUID id = UUID.randomUUID();
        String passwordHash = passwordEncoder.encode(normalized.password());
        try {
            organizers.insert(id, normalized.name(), normalized.email(), passwordHash);
            availability.createDefaultWeek(id);
        } catch (DataIntegrityViolationException exception) {
            if (DatabaseErrors.isOrganizerEmailConflict(exception)) {
                throw ApiException.emailAlreadyRegistered();
            }
            throw exception;
        }
        return OrganizerResponse.of(id, normalized.name(), normalized.email());
    }

    @Transactional(readOnly = true)
    public OrganizerResponse profile(UUID id) {
        return organizers.findProfile(id)
                .map(profile -> OrganizerResponse.of(profile.id(), profile.name(), profile.email()))
                .orElseThrow(ApiException::notFound);
    }
}
