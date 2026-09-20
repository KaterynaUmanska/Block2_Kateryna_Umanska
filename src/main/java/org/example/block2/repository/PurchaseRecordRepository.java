package org.example.block2.repository;

import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;
import org.example.block2.data.PurchaseRecordData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

/**
 * Repository for purchase records.
 */
@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecordData, Long>, JpaSpecificationExecutor<PurchaseRecordData> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = "material")
    Page<PurchaseRecordData> findAll(
            @Nullable Specification<PurchaseRecordData> spec,
            @NonNull Pageable pageable
    );
}


