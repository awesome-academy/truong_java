package com.sun.bookingtours.controller.web;

import com.sun.bookingtours.dto.request.TourRequest;
import com.sun.bookingtours.dto.request.TourScheduleRequest;
import com.sun.bookingtours.dto.request.TourStatusRequest;
import com.sun.bookingtours.dto.response.CategoryResponse;
import com.sun.bookingtours.dto.response.TourResponse;
import com.sun.bookingtours.dto.response.TourScheduleResponse;
import com.sun.bookingtours.entity.enums.TourStatus;
import com.sun.bookingtours.service.CategoryService;
import com.sun.bookingtours.service.TourScheduleService;
import com.sun.bookingtours.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/tours")
@RequiredArgsConstructor
public class AdminTourWebController {

    private final TourService tourService;
    private final TourScheduleService scheduleService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model) {
        TourStatus tourStatus = null;
        if (status != null && !status.isBlank()) {
            try { tourStatus = TourStatus.valueOf(status); } catch (IllegalArgumentException ignored) {}
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TourResponse> tours = tourService.adminList(tourStatus, pageable);

        List<Map<String, Object>> tourViews = tours.getContent().stream().map(this::toListView).toList();
        Page<Map<String, Object>> tourPage = new PageImpl<>(tourViews, tours.getPageable(), tours.getTotalElements());

        model.addAttribute("tours", tourPage);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", "tours");
        return "admin/tours/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("id", ""); empty.put("title", ""); empty.put("slug", "");
        empty.put("description", ""); empty.put("thumbnailUrl", "");
        empty.put("basePrice", ""); empty.put("durationDays", "1");
        empty.put("maxParticipants", "1"); empty.put("departureLocation", "");
        empty.put("categoryId", ""); empty.put("status", "DRAFT");
        model.addAttribute("tour", empty);
        model.addAttribute("categoryOptions", buildCategoryOptions());
        model.addAttribute("isEdit", false);
        model.addAttribute("currentPage", "tours");
        return "admin/tours/form";
    }

    @PostMapping
    public String create(@ModelAttribute TourRequest request, RedirectAttributes redirectAttrs) {
        try {
            TourResponse tour = tourService.create(request);
            redirectAttrs.addFlashAttribute("successMessage", "Tạo tour thành công.");
            return "redirect:/admin/tours/" + tour.getId();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/tours/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        TourResponse tour = tourService.getAdminDetail(id);
        List<TourScheduleResponse> rawSchedules = tour.getSchedules() != null ? tour.getSchedules() : List.of();
        List<Map<String, Object>> schedules = rawSchedules.stream().map(this::scheduleToMap).toList();
        model.addAttribute("tour", toDetailView(tour));
        model.addAttribute("schedules", schedules);
        model.addAttribute("currentPage", "tours");
        return "admin/tours/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        TourResponse tour = tourService.getAdminDetail(id);
        model.addAttribute("tour", toDetailView(tour));
        model.addAttribute("categoryOptions", buildCategoryOptions());
        model.addAttribute("isEdit", true);
        model.addAttribute("currentPage", "tours");
        return "admin/tours/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable UUID id,
                         @ModelAttribute TourRequest request,
                         RedirectAttributes redirectAttrs) {
        try {
            tourService.update(id, request);
            redirectAttrs.addFlashAttribute("successMessage", "Cập nhật tour thành công.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tours/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable UUID id,
                               @RequestParam TourStatus status,
                               RedirectAttributes redirectAttrs) {
        try {
            TourStatusRequest req = new TourStatusRequest();
            req.setStatus(status);
            tourService.updateStatus(id, req);
            redirectAttrs.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tours/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttrs) {
        try {
            tourService.delete(id);
            redirectAttrs.addFlashAttribute("successMessage", "Đã xóa tour.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tours";
    }

    // ---- Schedules ----

    @GetMapping("/{tourId}/schedules/new")
    public String newScheduleForm(@PathVariable UUID tourId, Model model) {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("id", ""); empty.put("departureDate", ""); empty.put("returnDate", "");
        empty.put("totalSlots", "1"); empty.put("priceOverride", ""); empty.put("status", "OPEN");
        model.addAttribute("tourId", tourId.toString());
        model.addAttribute("schedule", empty);
        model.addAttribute("isEdit", false);
        model.addAttribute("currentPage", "tours");
        return "admin/tours/schedule_form";
    }

    @PostMapping("/{tourId}/schedules")
    public String createSchedule(@PathVariable UUID tourId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                                 @RequestParam int totalSlots,
                                 @RequestParam(required = false) BigDecimal priceOverride,
                                 RedirectAttributes redirectAttrs) {
        try {
            // TourScheduleRequest là record — construct trực tiếp từ @RequestParam
            // Compact constructor của record sẽ validate returnDate > departureDate
            TourScheduleRequest req = new TourScheduleRequest(departureDate, returnDate, totalSlots, priceOverride);
            scheduleService.create(tourId, req);
            redirectAttrs.addFlashAttribute("successMessage", "Tạo lịch khởi hành thành công.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tours/" + tourId;
    }

    @GetMapping("/{tourId}/schedules/{scheduleId}/edit")
    public String editScheduleForm(@PathVariable UUID tourId,
                                   @PathVariable UUID scheduleId,
                                   Model model) {
        TourScheduleResponse schedule = scheduleService.getDetail(scheduleId);
        model.addAttribute("tourId", tourId.toString());
        model.addAttribute("schedule", scheduleToMap(schedule));
        model.addAttribute("isEdit", true);
        model.addAttribute("currentPage", "tours");
        return "admin/tours/schedule_form";
    }

    @PostMapping("/{tourId}/schedules/{scheduleId}/edit")
    public String updateSchedule(@PathVariable UUID tourId,
                                 @PathVariable UUID scheduleId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                                 @RequestParam int totalSlots,
                                 @RequestParam(required = false) BigDecimal priceOverride,
                                 RedirectAttributes redirectAttrs) {
        try {
            TourScheduleRequest req = new TourScheduleRequest(departureDate, returnDate, totalSlots, priceOverride);
            scheduleService.update(scheduleId, req);
            redirectAttrs.addFlashAttribute("successMessage", "Cập nhật lịch thành công.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/tours/" + tourId;
    }

    // ---- private helpers ----

    private Map<String, Object> toListView(TourResponse t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId().toString());
        m.put("title", t.getTitle());
        m.put("slug", t.getSlug());
        m.put("status", t.getStatus().name());
        m.put("basePrice", t.getBasePrice());
        m.put("durationDays", t.getDurationDays());
        m.put("categoryName", t.getCategory() != null ? t.getCategory().getName() : "—");
        m.put("createdAt", t.getCreatedAt());
        return m;
    }

    private Map<String, Object> toDetailView(TourResponse t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId().toString());
        m.put("title", t.getTitle());
        m.put("slug", t.getSlug());
        m.put("description", t.getDescription() != null ? t.getDescription() : "");
        m.put("thumbnailUrl", t.getThumbnailUrl() != null ? t.getThumbnailUrl() : "");
        m.put("basePrice", t.getBasePrice());
        m.put("durationDays", t.getDurationDays());
        m.put("maxParticipants", t.getMaxParticipants());
        m.put("departureLocation", t.getDepartureLocation() != null ? t.getDepartureLocation() : "");
        m.put("status", t.getStatus().name());
        m.put("categoryId", t.getCategory() != null ? t.getCategory().getId().toString() : "");
        m.put("categoryName", t.getCategory() != null ? t.getCategory().getName() : "—");
        m.put("createdAt", t.getCreatedAt());
        return m;
    }

    private Map<String, Object> scheduleToMap(TourScheduleResponse s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.id().toString());
        m.put("departureDate", s.departureDate() != null ? s.departureDate().toString() : "");
        m.put("returnDate", s.returnDate() != null ? s.returnDate().toString() : "");
        m.put("totalSlots", s.totalSlots());
        m.put("priceOverride", s.priceOverride() != null ? s.priceOverride().toPlainString() : "");
        m.put("status", s.status().name());
        return m;
    }

    // Flatten category tree để dùng trong dropdown
    private List<Map<String, Object>> buildCategoryOptions() {
        List<Map<String, Object>> options = new ArrayList<>();
        buildCategoryOptions(categoryService.getTree(), options, 0);
        return options;
    }

    private void buildCategoryOptions(List<CategoryResponse> nodes, List<Map<String, Object>> result, int depth) {
        for (CategoryResponse cat : nodes) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", cat.getId().toString());
            m.put("label", "—".repeat(depth) + " " + cat.getName());
            result.add(m);
            if (cat.getChildren() != null) {
                buildCategoryOptions(cat.getChildren(), result, depth + 1);
            }
        }
    }
}
