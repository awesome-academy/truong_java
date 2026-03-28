package com.sun.bookingtours.controller.web;

import com.sun.bookingtours.dto.request.LoginRequest;
import com.sun.bookingtours.dto.response.AuthResponse;
import com.sun.bookingtours.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminLoginWebController {

    private final AuthService authService;

    @GetMapping({"", "/"})
    public String root() {
        return "redirect:/admin/users";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("currentPage", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng, hoặc tài khoản không có quyền admin.");
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletResponse response) {
        try {
            LoginRequest request = new LoginRequest();
            request.setEmail(email);
            request.setPassword(password);

            AuthResponse auth = authService.loginAdmin(request);

            Cookie cookie = new Cookie("admin_token", auth.getAccessToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/admin");
            cookie.setMaxAge(3600); // 1 giờ
            response.addCookie(cookie);

            return "redirect:/admin/users";
        } catch (Exception e) {
            return "redirect:/admin/login?error=true";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("admin_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/admin");
        cookie.setMaxAge(0); // xóa cookie
        response.addCookie(cookie);
        return "redirect:/admin/login";
    }
}
