package com.hunts.bail.repository;

import com.hunts.bail.domain.User;
import com.hunts.bail.enums.Role;
import com.hunts.bail.exception.RecordPersistenceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of UserRepository.
 */
public class SqliteUserRepository implements UserRepository {

    @Override
    public void save(User user) {
        String sql = """
            INSERT INTO users (user_id, username, password_hash, role, full_name)
            VALUES (?,?,?,?,?)
        """;
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getFullName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to save user: " + e.getMessage());
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return queryOne("SELECT * FROM users WHERE username = ?", username);
    }

    @Override
    public Optional<User> findById(String userId) {
        return queryOne("SELECT * FROM users WHERE user_id = ?", userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to check username: " + e.getMessage());
        }
    }

    @Override
    public void delete(String userId) {
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM users WHERE user_id = ?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to delete user: " + e.getMessage());
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM users ORDER BY full_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(hydrate(rs));
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to list users: " + e.getMessage());
        }
        return list;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Optional<User> queryOne(String sql, String param) {
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(hydrate(rs));
            }
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to query user: " + e.getMessage());
        }
        return Optional.empty();
    }

    private User hydrate(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getString("full_name"));
    }
}
