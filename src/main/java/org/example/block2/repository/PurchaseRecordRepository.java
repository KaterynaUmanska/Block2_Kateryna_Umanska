package org.example.block2.repository;

import org.example.block2.data.PurchaseRecordData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for purchase records.
 */
@Repository
public interface PurchaseRecordRepository extends
        JpaRepository<PurchaseRecordData, Long>,
        JpaSpecificationExecutor<PurchaseRecordData> {
}


