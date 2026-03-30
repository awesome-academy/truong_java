package com.sun.bookingtours.config;

import com.sun.bookingtours.security.AdminCookieAuthFilter;
import com.sun.bookingtours.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AdminCookieAuthFilter adminCookieAuthFilter;

    /*
     * CookieCsrfTokenRepository — lưu CSRF token trong cookie XSRF-TOKEN, hoạt động với STATELESS
     *                             Thymeleaf tự inject _csrf hidden field vào form có th:action
     *                             /api/** được exempt vì REST dùng JWT (stateless, không cần CSRF)
     * STATELESS               — Spring không tạo session, mỗi request tự xác thực qua JWT hoặc cookie
     * permitAll()             — Không cần token, ai cũng gọi được
     * hasRole("ADMIN")        — Spring tự check ROLE_ADMIN trong authorities của UserPrincipal
     * addFilterBefore()       — Đặt custom filter chạy trước UsernamePasswordAuthenticationFilter
     * PasswordEncoder         — BCrypt hash password khi register, verify khi login
     * AuthenticationManager   — Spring dùng để xác thực username/password lúc login
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // REST API dùng JWT nên không cần CSRF. Admin UI dùng cookie → cần CSRF.
                // CookieCsrfTokenRepository hoạt động với STATELESS session (không cần HttpSession).
                // Thymeleaf tự inject _csrf hidden field vào mọi form có th:action.
                .csrfTokenRepository(new CookieCsrfTokenRepository())
                .ignoringRequestMatchers("/api/**"))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                // Admin web routes: redirect về login page thay vì trả 401 JSON
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/admin/login"),
                    request -> request.getServletPath().startsWith("/admin")
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()  // Spring Boot forward lỗi unhandled về /error — phải permit
                // Các endpoint /me phải đứng trước rule permitAll bên dưới
                // vì Spring Security match rule đầu tiên thắng
                .requestMatchers(HttpMethod.GET, "/api/reviews/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/tours/*/ratings/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/tours/**", "/api/places/**",
                        "/api/foods/**", "/api/news/**", "/api/reviews/**",
                        "/api/categories/**", "/api/comments").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(adminCookieAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
