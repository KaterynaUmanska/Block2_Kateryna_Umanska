package org.example.block2.repository;

import org.example.block2.data.PurchaseRecordData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for purchase records.
 */
@Repository
public  interface PurchaseRecordRepository extends JpaRepository<PurchaseRecordData, Long>{
    /**
     * Finds purchase records using optional filters with pagination.
     *
     * @param orderId optional order ID
     * @param materialName optional material name
     * @param quantityFrom optional min quantity
     * @param quantityTo optional max quantity
     * @param pageable pagination parameters
     * @return page of purchase records
     */
    @Query("""
            SELECT p
            FROM PurchaseRecordData p
            JOIN p.material m
            WHERE (:orderId IS NULL OR p.orderId = :orderId)
              AND (:materialName IS NULL OR m.name = :materialName)
              AND (:quantityFrom IS NULL OR p.quantity >= :quantityFrom)
              AND (:quantityTo IS NULL OR p.quantity <= :quantityTo)
            """)
    Page<PurchaseRecordData> findByFilters(
            @Param("orderId") Long orderId,
            @Param("materialName") String materialName,
            @Param("quantityFrom") Double quantityFrom,
            @Param("quantityTo") Double quantityTo,
            Pageable pageable
    );

    /**
     * Finds all purchase records using optional filters.
     *
     * @param orderId      optional order ID
     * @param materialName optional material name
     * @param quantityFrom optional minimum quantity
     * @param quantityTo   optional maximum quantity
     * @return list of filtered purchase records
     */
    @Query("""
        SELECT p
        FROM PurchaseRecordData p
        JOIN p.material m
        WHERE (:orderId IS NULL OR p.orderId = :orderId)
          AND (:materialName IS NULL OR m.name = :materialName)
          AND (:quantityFrom IS NULL OR p.quantity >= :quantityFrom)
          AND (:quantityTo IS NULL OR p.quantity <= :quantityTo)
        """)
    List<PurchaseRecordData> findAllByFilters(
            @Param("orderId") Long orderId,
            @Param("materialName") String materialName,
            @Param("quantityFrom") Double quantityFrom,
            @Param("quantityTo") Double quantityTo
    );

}
