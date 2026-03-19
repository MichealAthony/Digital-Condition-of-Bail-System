package com.hunts.bail.repository;

import com.hunts.bail.domain.BailRecord;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BailRecord persistence operations.
 *
 */
public interface BailRecordRepository {

    /**
     * Persists a new bail record.
     *
     * @param record the BailRecord to save
     * @throws com.hunts.bail.exception.RecordPersistenceException on failure
     */
    void save(BailRecord record);

    /**
     * Checks whether a bail record with the given ID already exists.
     */
    boolean existsById(String bailId);

    /**
     * Retrieves a bail record by its unique ID.
     */
    Optional<BailRecord> findById(String bailId);

    /**
     * Returns all bail records in the system.
     */
    List<BailRecord> findAll();

    /**
     * Updates an existing bail record (used by UC6 – Update Bail Record).
     */
    void update(BailRecord record);

    /**
     * Permanently removes a bail record (used by UC5 – Delete Bail Record).
     */
    void delete(String bailId);
}
