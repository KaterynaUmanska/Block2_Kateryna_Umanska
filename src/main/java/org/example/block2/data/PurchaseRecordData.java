package org.example.block2.data;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Purchase record persistent entity.
 */
@Getter
@Setter
@Entity
@Table(name = "purchase_records")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PurchaseRecordData {
    /**
     * Purchase record ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * ID of the purchase order.
     */
    @Column(nullable = false)
    private Long orderId;

    /**
     * Material associated with this purchase record.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private MaterialData material;

    /**
     * Required material quantity.
     */
    @Column(nullable = false)
    private Double quantity;

}
