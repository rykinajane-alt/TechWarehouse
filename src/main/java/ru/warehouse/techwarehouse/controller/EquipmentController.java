
package ru.warehouse.techwarehouse.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.warehouse.techwarehouse.entity.Equipment;
import ru.warehouse.techwarehouse.service.EquipmentService;
import ru.warehouse.techwarehouse.dto.DailyDispatchStat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    // Список техники, поиск и сортировка
    @GetMapping
    public String getAllEquipment(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        List<Equipment> allEquipment =
                equipmentService.getAllEquipment();

        List<Equipment> equipmentList = allEquipment;

        // Поиск по виду техники и группе
        if (keyword != null && !keyword.isBlank()) {
            String search = keyword.trim().toLowerCase(Locale.ROOT);

            equipmentList = equipmentList.stream()
                    .filter(e ->
                            (e.getEquipmentType() != null
                                    && e.getEquipmentType()
                                    .toLowerCase(Locale.ROOT)
                                    .contains(search))
                                    ||
                                    (e.getEquipmentGroup() != null
                                            && e.getEquipmentGroup()
                                            .toLowerCase(Locale.ROOT)
                                            .contains(search)))
                    .toList();
        }

        // Сортировка сначала по дате ввоза, затем по виду техники
        Comparator<Equipment> comparator =
                Comparator.comparing(
                                Equipment::getArrivalDate,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(
                                Equipment::getEquipmentType,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER));

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        equipmentList = equipmentList.stream()
                .sorted(comparator)
                .toList();

        model.addAttribute("equipmentList", equipmentList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("recordCount",
                equipmentService.countEquipment());

        return "equipment";
    }

    // Форма добавления
    @GetMapping("/new")
    public String newEquipmentForm(Model model) {
        model.addAttribute("equipment", new Equipment());
        model.addAttribute("pageTitle", "Добавление техники");
        return "equipment-form";
    }

    // Форма редактирования
    @GetMapping("/edit/{id}")
    public String editEquipmentForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "equipment",
                equipmentService.getEquipmentById(id));
        model.addAttribute(
                "pageTitle",
                "Редактирование техники");

        return "equipment-form";
    }

    // Сохранение записи
    @PostMapping("/save")
    public String saveEquipment(
            @Valid @ModelAttribute("equipment") Equipment equipment,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    equipment.getId() == null
                            ? "Добавление техники"
                            : "Редактирование техники");

            return "equipment-form";
        }

        equipmentService.saveEquipment(equipment);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Запись успешно сохранена");

        return "redirect:/equipment";
    }

    // Удаление записи
    @PostMapping("/delete/{id}")
    public String deleteEquipment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        equipmentService.deleteEquipment(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Запись успешно удалена");

        return "redirect:/equipment";
    }

    // Статистика отгрузок за последние 30 дней
    @GetMapping("/statistics")
    public String getStatistics(Model model) {

        List<Equipment> allEquipment =
                equipmentService.getAllEquipment();

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);

        // Учитываем только записи с датой вывоза в выбранном периоде
        Map<LocalDate, Long> countsByDate = allEquipment.stream()
                .filter(e -> e.getDispatchDate() != null)
                .filter(e -> {
                    LocalDate date = e.getDispatchDate();
                    return !date.isBefore(startDate)
                            && !date.isAfter(today);
                })
                .collect(Collectors.groupingBy(
                        Equipment::getDispatchDate,
                        Collectors.counting()));

        long maxCount = countsByDate.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM");

        List<DailyDispatchStat> dailyStats =
                IntStream.range(0, 30)
                        .mapToObj(i -> {
                            LocalDate date = startDate.plusDays(i);
                            long count = countsByDate.getOrDefault(
                                    date, 0L);

                            int percent = maxCount == 0
                                    ? 0
                                    : (int) Math.round(
                                    count * 100.0 / maxCount);

                            return new DailyDispatchStat(
                                    date.format(formatter),
                                    count,
                                    percent);
                        })
                        .toList();

        long totalInPeriod = countsByDate.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        model.addAttribute("dailyStats", dailyStats);
        model.addAttribute("totalInPeriod", totalInPeriod);
        model.addAttribute("startDate", startDate);
        model.addAttribute("today", today);

        return "statistics";
    }
}
