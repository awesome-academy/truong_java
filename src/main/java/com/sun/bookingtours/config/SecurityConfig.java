package com.sun.bookingtours.config;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /*
     * csrf.disable()          — REST API dùng JWT nên không cần CSRF protection
     * STATELESS               — Spring không tạo session, mỗi request tự xác thực qua JWT
     * permitAll()             — Không cần token, ai cũng gọi được
     * hasRole("ADMIN")        — Spring tự check ROLE_ADMIN trong authorities của UserPrincipal
     * addFilterBefore()       — Đặt JwtAuthenticationFilter chạy trước filter mặc định của Spring
     * PasswordEncoder         — BCrypt hash password khi register, verify khi login
     * AuthenticationManager   — Spring dùng để xác thực username/password lúc login
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
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
