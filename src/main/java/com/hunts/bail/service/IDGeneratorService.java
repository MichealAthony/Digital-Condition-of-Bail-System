package com.hunts.bail.service;

import com.hunts.bail.repository.BailRecordRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique, human-readable bail IDs in the format:
 *   BAIL-YYYYMMDD-NNNN   e.g.  BAIL-20260218-0001
 *
    * The sequence number resets each day. The service checks the repository to ensure generated IDs are unique, guarding against collisions.
 */
public class IDGeneratorService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger sequence = new AtomicInteger(0);
    private final BailRecordRepository repository;

    public IDGeneratorService(BailRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * Generates a unique bail ID, verifying uniqueness against the repository.
     *
     * @return a unique bail ID string
     */
    public String generateBailId() {
        String candidate;
        do {
            int seq = sequence.incrementAndGet();
            String date = LocalDate.now().format(DATE_FMT);
            candidate = String.format("BAIL-%s-%04d", date, seq);
        } while (repository.existsById(candidate));   // guard against collision
        return candidate;
    }
}
