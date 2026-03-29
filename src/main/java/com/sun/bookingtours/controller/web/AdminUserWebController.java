package com.sun.bookingtours.controller.web;

import com.sun.bookingtours.dto.response.AdminUserDetailResponse;
import com.sun.bookingtours.dto.response.AdminUserResponse;
import com.sun.bookingtours.entity.enums.Role;
import com.sun.bookingtours.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserWebController {

    private final AdminUserService adminUserService;

    @GetMapping
    public String listUsers(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) Boolean isActive,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            Model model) {

        Role roleEnum = null;
        if (role != null && !role.isBlank()) {
            try { roleEnum = Role.valueOf(role); } catch (IllegalArgumentException ignored) {}
        }
        Page<AdminUserResponse> users = adminUserService.listUsers(
                roleEnum, isActive, search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        // Spring Framework 7 hạn chế SpEL truy cập property trên Lombok @Data class.
        // Convert sang Map để Thymeleaf đọc được qua MapAccessor (không bị restricted).
        List<Map<String, Object>> userViews = users.getContent().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId().toString());
            m.put("fullName", u.getFullName());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("role", u.getRole().name());
            m.put("active", u.isActive());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();

        Page<Map<String, Object>> userPage = new PageImpl<>(userViews, users.getPageable(), users.getTotalElements());

        model.addAttribute("users", userPage);
        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("isActive", isActive);
        model.addAttribute("currentPage", "users");

        return "admin/users/list";
    }

    @GetMapping("/{id}")
    public String userDetail(@PathVariable UUID id, Model model) {
        AdminUserDetailResponse user = adminUserService.getUserDetail(id);

        Map<String, Object> userView = new LinkedHashMap<>();
        userView.put("id", user.getId().toString());
        userView.put("fullName", user.getFullName());
        userView.put("email", user.getEmail());
        userView.put("phone", user.getPhone());
        userView.put("role", user.getRole().name());
        userView.put("active", user.isActive());
        userView.put("createdAt", user.getCreatedAt());

        List<Map<String, Object>> bookingViews = user.getRecentBookings().stream().map(b -> {
            Map<String, Object> bm = new LinkedHashMap<>();
            bm.put("bookingCode", b.bookingCode());
            bm.put("tourTitle", b.tourTitle());
            bm.put("departureDate", b.departureDate());
            bm.put("numParticipants", b.numParticipants());
            bm.put("totalAmount", b.totalAmount());
            bm.put("status", b.status().name());
            return bm;
        }).toList();
        userView.put("recentBookings", bookingViews);

        model.addAttribute("user", userView);
        model.addAttribute("currentPage", "users");
        return "admin/users/detail";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable UUID id, RedirectAttributes redirectAttrs) {
        adminUserService.activate(id);
        redirectAttrs.addFlashAttribute("successMessage", "Tài khoản đã được kích hoạt.");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable UUID id, RedirectAttributes redirectAttrs) {
        adminUserService.deactivate(id);
        redirectAttrs.addFlashAttribute("successMessage", "Tài khoản đã bị khóa.");
        return "redirect:/admin/users/" + id;
    }
}
