package com.sun.bookingtours.controller.web;

import com.sun.bookingtours.dto.request.CategoryRequest;
import com.sun.bookingtours.dto.response.CategoryResponse;
import com.sun.bookingtours.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryWebController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        List<CategoryResponse> tree = categoryService.getTree();
        List<Map<String, Object>> flatList = new ArrayList<>();
        flattenTree(tree, flatList, 0);

        model.addAttribute("categories", flatList);
        model.addAttribute("currentPage", "categories");
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        List<Map<String, Object>> parentOptions = buildParentOptions(null);
        model.addAttribute("parentOptions", parentOptions);
        model.addAttribute("category", Map.of("id", "", "name", "", "slug", "",
                "description", "", "imageUrl", "", "parentId", ""));
        model.addAttribute("isEdit", false);
        model.addAttribute("currentPage", "categories");
        return "admin/categories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CategoryRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            redirectAttrs.addFlashAttribute("errorMessage",
                    bindingResult.getFieldErrors().stream()
                            .map(e -> e.getField() + ": " + e.getDefaultMessage())
                            .findFirst().orElse("Dữ liệu không hợp lệ."));
            return "redirect:/admin/categories/new";
        }
        try {
            categoryService.create(request);
            redirectAttrs.addFlashAttribute("successMessage", "Tạo category thành công.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        Map<String, Object> category = categoryService.getCategoryForEdit(id);
        // getTree() được gọi 1 lần duy nhất, dùng chung cho cả build options
        List<CategoryResponse> tree = categoryService.getTree();
        List<Map<String, Object>> parentOptions = new ArrayList<>();
        buildOptions(tree, parentOptions, 0, id.toString());
        model.addAttribute("category", category);
        model.addAttribute("parentOptions", parentOptions);
        model.addAttribute("isEdit", true);
        model.addAttribute("currentPage", "categories");
        return "admin/categories/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute CategoryRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            redirectAttrs.addFlashAttribute("errorMessage",
                    bindingResult.getFieldErrors().stream()
                            .map(e -> e.getField() + ": " + e.getDefaultMessage())
                            .findFirst().orElse("Dữ liệu không hợp lệ."));
            return "redirect:/admin/categories/" + id + "/edit";
        }
        try {
            categoryService.update(id, request);
            redirectAttrs.addFlashAttribute("successMessage", "Cập nhật category thành công.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable UUID id, RedirectAttributes redirectAttrs) {
        try {
            categoryService.delete(id);
            redirectAttrs.addFlashAttribute("successMessage", "Đã deactivate category.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ---- helpers ----

    // Flatten cây category → list phẳng với field depth để render indent trong template.
    // Tránh đệ quy trong Thymeleaf vì template recursion phức tạp và dễ lỗi.
    private void flattenTree(List<CategoryResponse> nodes, List<Map<String, Object>> result, int depth) {
        for (CategoryResponse cat : nodes) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", cat.getId().toString());
            m.put("name", cat.getName());
            m.put("slug", cat.getSlug());
            m.put("active", cat.isActive());
            m.put("depth", depth);
            m.put("hasChildren", cat.getChildren() != null && !cat.getChildren().isEmpty());
            result.add(m);
            if (cat.getChildren() != null) {
                flattenTree(cat.getChildren(), result, depth + 1);
            }
        }
    }

    // Build flat list cho dropdown chọn parent. excludeId để tránh category tự chọn mình làm cha.
    private List<Map<String, Object>> buildParentOptions(String excludeId) {
        List<CategoryResponse> tree = categoryService.getTree();
        List<Map<String, Object>> options = new ArrayList<>();
        buildOptions(tree, options, 0, excludeId);
        return options;
    }

    private void buildOptions(List<CategoryResponse> nodes, List<Map<String, Object>> result,
                              int depth, String excludeId) {
        for (CategoryResponse cat : nodes) {
            if (cat.getId().toString().equals(excludeId)) continue; // bỏ chính nó
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", cat.getId().toString());
            // Thêm indent vào tên để dropdown dễ đọc
            m.put("label", "—".repeat(depth) + " " + cat.getName());
            result.add(m);
            if (cat.getChildren() != null) {
                buildOptions(cat.getChildren(), result, depth + 1, excludeId);
            }
        }
    }
}
