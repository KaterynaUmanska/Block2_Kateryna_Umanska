package org.example.block2.data;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.block2.dict.Unit;

import java.util.Set;

/**
 * Material persistent entity.
 */
@Getter
@Setter
@Entity
@Table(name = "materials")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MaterialData {

    /**
     * Material ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Material name.
     * Unique value.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Material description.
     */
    @Column(length = 1000)
    private String description;

    /**
     * Unit of measurement.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

}
