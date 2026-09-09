package org.example.block2.repository;

import org.example.block2.data.MaterialData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for materials.
 */
public interface MaterialRepository extends JpaRepository<MaterialData, Long> {
    /**
     * Finds material by name.
     *
     * @param name material name
     * @return material if found
     */
    Optional<MaterialData> findByNameIgnoreCase(String name);

    /**
     * Checks whether material with the given name exists.
     *
     * @param name material name
     * @return true if material exists
     */
    boolean existsByNameIgnoreCase(String name);
}
