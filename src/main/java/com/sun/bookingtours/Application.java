package com.sun.bookingtours;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// VIA_DTO: serialize Page<T> qua DTO trung gian thay vì PageImpl trực tiếp
// → cấu trúc JSON pagination ổn định, không thay đổi khi upgrade Spring Data
// → response gọn hơn: chỉ còn { content, page: { size, number, totalElements, totalPages } }
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
// EnableScheduling kích hoạt @Scheduled — dùng cho RevokedAccessTokenService.cleanupExpired()
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
