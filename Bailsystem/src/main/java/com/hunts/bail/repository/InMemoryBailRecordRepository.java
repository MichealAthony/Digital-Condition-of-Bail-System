package com.hunts.bail.repository;

import com.hunts.bail.domain.BailRecord;
import com.hunts.bail.exception.RecordPersistenceException;

import java.util.*;

/**
 * In-memory implementation of BailRecordRepository.
 *
 * Simulates a database using a HashMap. 
 */
public class InMemoryBailRecordRepository implements BailRecordRepository {

    private final Map<String, BailRecord> store = new LinkedHashMap<>();

    @Override
    public void save(BailRecord record) {
        Objects.requireNonNull(record, "Cannot save a null BailRecord.");
        if (store.containsKey(record.getBailId())) {
            throw new RecordPersistenceException(
                    "A record with bail ID " + record.getBailId() + " already exists.");
        }
        store.put(record.getBailId(), record);
    }

    @Override
    public boolean existsById(String bailId) {
        return store.containsKey(bailId);
    }

    @Override
    public Optional<BailRecord> findById(String bailId) {
        return Optional.ofNullable(store.get(bailId));
    }

    @Override
    public List<BailRecord> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    @Override
    public void update(BailRecord record) {
        Objects.requireNonNull(record);
        if (!store.containsKey(record.getBailId())) {
            throw new RecordPersistenceException(
                    "Cannot update: no record found with bail ID " + record.getBailId());
        }
        store.put(record.getBailId(), record);
    }

    @Override
    public void delete(String bailId) {
        if (!store.containsKey(bailId)) {
            throw new RecordPersistenceException(
                    "Cannot delete: no record found with bail ID " + bailId);
        }
        store.remove(bailId);
    }
}
