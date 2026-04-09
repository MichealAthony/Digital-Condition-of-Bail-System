package com.hunts.bail.domain;

import com.hunts.bail.domain.*;
import com.hunts.bail.enums.BailStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reconstructs a BailRecord aggregate from persisted data.
 *
 * BailRecordFactory sets createdAt = LocalDateTime.now() and status = ACTIVE,
 * which is correct for new records but wrong when loading from the database.
 * This rehydrator restores the exact timestamps and status that were persisted.
 *
 * Lives in the domain package so it can access the package-private
 * BailRecord.forRehydration() constructor and attachReportingEntry() method.
 * Only SqliteBailRecordRepository should call this class.
 */
public class BailRecordRehydrator {

    private BailRecordRehydrator() {}

    public static BailRecord rehydrate(String bailId,
                                 AccusedPerson accused,
                                 List<BailCondition> conditions,
                                 String suretyInfo,
                                 BailStatus status,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt,
                                 List<ReportingEntry> reportingLog) {

        // BailRecord constructor is package-private in domain, so we can only call it from here.
        
        BailRecord record = BailRecord.forRehydration(
                bailId, accused, conditions, suretyInfo,
                status, createdAt, updatedAt);

        // Re-attach reporting entries directly
        for (ReportingEntry entry : reportingLog) {
            record.attachReportingEntry(entry);
        }
        return record;
    }
}
