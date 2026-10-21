
package ru.warehouse.techwarehouse.service;

import org.springframework.stereotype.Service;
import ru.warehouse.techwarehouse.entity.Equipment;
import ru.warehouse.techwarehouse.repository.EquipmentRepository;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    // Получение всех записей
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    // Получение записи по идентификатору
    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Запись о технике не найдена"));
    }

    // Сохранение новой или изменённой записи
    public Equipment saveEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    // Удаление записи
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    // Подсчёт общего количества записей
    public long countEquipment() {
        return equipmentRepository.count();
    }
}
