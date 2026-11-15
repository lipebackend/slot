package dev.slot.organizer;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrganizerRepository {
    private final JdbcClient jdbc;

    public OrganizerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(UUID id, String name, String email, String passwordHash) {
        jdbc.sql("""
                INSERT INTO organizer (id, name, email, password_hash)
                VALUES (:id, :name, :email, :passwordHash)
                """)
                .param("id", id)
                .param("name", name)
                .param("email", email)
                .param("passwordHash", passwordHash)
                .update();
    }

    public Optional<Account> findAccountByEmail(String email) {
        return jdbc.sql("""
                SELECT id, name, email, password_hash
                FROM organizer
                WHERE email = :email
                """)
                .param("email", email)
                .query((rs, row) -> new Account(
                        rs.getObject("id", UUID.class),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash")))
                .optional();
    }

    public Optional<Profile> findProfile(UUID id) {
        return jdbc.sql("""
                SELECT id, name, email
                FROM organizer
                WHERE id = :id
                """)
                .param("id", id)
                .query((rs, row) -> new Profile(
                        rs.getObject("id", UUID.class),
                        rs.getString("name"),
                        rs.getString("email")))
                .optional();
    }

    public boolean lock(UUID id) {
        return jdbc.sql("SELECT id FROM organizer WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query((rs, row) -> rs.getObject("id", UUID.class))
                .optional()
                .isPresent();
    }

    public static final class Account {
        private final UUID id;
        private final String name;
        private final String email;
        private final String passwordHash;

        Account(UUID id, String name, String email, String passwordHash) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.passwordHash = passwordHash;
        }

        public UUID id() {
            return id;
        }

        public String name() {
            return name;
        }

        public String email() {
            return email;
        }

        public String passwordHash() {
            return passwordHash;
        }

        @Override
        public String toString() {
            return "Account[" + id + "]";
        }
    }

    public record Profile(UUID id, String name, String email) {
    }
}
