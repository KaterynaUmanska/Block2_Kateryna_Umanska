package org.example.block2.repository;

import org.example.block2.data.PurchaseRecordData;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class PurchaseRecordSpecifications {
    public static Specification<PurchaseRecordData> hasOrderId(Long orderId) {
        return (root, query, cb) ->
                orderId == null ? null : cb.equal(root.get("orderId"), orderId);
    }

    public static Specification<PurchaseRecordData> hasMaterialName(String materialName) {
        return (root, query, cb) ->
                materialName == null ? null : cb.equal(cb.lower(root.get("material").get("name")), materialName.toLowerCase());
    }

    public static Specification<PurchaseRecordData> quantityGreaterThanOrEqualTo(BigDecimal quantityFrom) {
        return (root, query, cb) ->
                quantityFrom == null ? null : cb.greaterThanOrEqualTo(root.get("quantity"), quantityFrom);
    }

    public static Specification<PurchaseRecordData> quantityLessThanOrEqualTo(BigDecimal quantityTo) {
        return (root, query, cb) ->
                quantityTo == null ? null : cb.lessThanOrEqualTo(root.get("quantity"), quantityTo);
    }
}
