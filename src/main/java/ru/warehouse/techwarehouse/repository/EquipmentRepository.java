
package ru.warehouse.techwarehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.warehouse.techwarehouse.entity.Equipment;

public interface EquipmentRepository
        extends JpaRepository<Equipment, Long> {
}
