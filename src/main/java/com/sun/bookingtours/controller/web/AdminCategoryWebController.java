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

    record CategoryFlatItem(String id, String name, String slug, boolean active, int depth, boolean hasChildren) {}
    record CategoryOption(String id, String label) {}

    @GetMapping
    public String list(Model model) {
        List<CategoryResponse> tree = categoryService.getTree();
        List<CategoryFlatItem> flatList = new ArrayList<>();
        flattenTree(tree, flatList, 0);

        model.addAttribute("categories", flatList);
        model.addAttribute("currentPage", "categories");
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        List<CategoryOption> parentOptions = new ArrayList<>();
        buildOptions(categoryService.getTree(), parentOptions, 0, null);
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
                         Model model,
                         RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            List<CategoryOption> parentOptions = new ArrayList<>();
            buildOptions(categoryService.getTree(), parentOptions, 0, null);
            model.addAttribute("parentOptions", parentOptions);
            model.addAttribute("category", formDataFrom("", request));
            model.addAttribute("isEdit", false);
            model.addAttribute("currentPage", "categories");
            model.addAttribute("errorMessage", bindingResult.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .findFirst().orElse("Dữ liệu không hợp lệ."));
            return "admin/categories/form";
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
        List<CategoryOption> parentOptions = new ArrayList<>();
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
                         Model model,
                         RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            List<CategoryOption> parentOptions = new ArrayList<>();
            buildOptions(categoryService.getTree(), parentOptions, 0, id.toString());
            model.addAttribute("parentOptions", parentOptions);
            model.addAttribute("category", formDataFrom(id.toString(), request));
            model.addAttribute("isEdit", true);
            model.addAttribute("currentPage", "categories");
            model.addAttribute("errorMessage", bindingResult.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .findFirst().orElse("Dữ liệu không hợp lệ."));
            return "admin/categories/form";
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
    private void flattenTree(List<CategoryResponse> nodes, List<CategoryFlatItem> result, int depth) {
        for (CategoryResponse cat : nodes) {
            result.add(new CategoryFlatItem(
                    cat.getId().toString(),
                    cat.getName(),
                    cat.getSlug(),
                    cat.isActive(),
                    depth,
                    cat.getChildren() != null && !cat.getChildren().isEmpty()
            ));
            if (cat.getChildren() != null) {
                flattenTree(cat.getChildren(), result, depth + 1);
            }
        }
    }

    private void buildOptions(List<CategoryResponse> nodes, List<CategoryOption> result,
                              int depth, String excludeId) {
        for (CategoryResponse cat : nodes) {
            if (cat.getId().toString().equals(excludeId)) continue; // bỏ chính nó
            // Thêm indent vào tên để dropdown dễ đọc
            result.add(new CategoryOption(
                    cat.getId().toString(),
                    "—".repeat(depth) + " " + cat.getName()
            ));
            if (cat.getChildren() != null) {
                buildOptions(cat.getChildren(), result, depth + 1, excludeId);
            }
        }
    }

    // Build Map category để truyền vào form khi validation fail — giữ lại data user đã nhập.
    // parentId được convert sang String để Thymeleaf so sánh đúng với opt.id (cũng là String).
    private Map<String, Object> formDataFrom(String id, CategoryRequest request) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", request.getName() != null ? request.getName() : "");
        m.put("slug", request.getSlug() != null ? request.getSlug() : "");
        m.put("description", request.getDescription() != null ? request.getDescription() : "");
        m.put("imageUrl", request.getImageUrl() != null ? request.getImageUrl() : "");
        m.put("parentId", request.getParentId() != null ? request.getParentId().toString() : "");
        return m;
    }
}
