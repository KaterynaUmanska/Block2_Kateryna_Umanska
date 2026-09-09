package org.example.block2.repository;

import org.example.block2.data.PurchaseRecordData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for purchase records.
 */
@Repository
public interface PurchaseRecordRepository extends
        JpaRepository<PurchaseRecordData, Long>,
        JpaSpecificationExecutor<PurchaseRecordData> {

}


