package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.UserBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBankAccountRepository extends JpaRepository<UserBankAccount, UUID> {

    // Spring Data tự generate SQL từ tên method:
    // findBy + UserId → WHERE user_id = ?
    List<UserBankAccount> findByUserId(UUID userId);

    // Tìm 1 bank account theo id VÀ user_id
    // → đảm bảo user chỉ thao tác được account của chính mình
    Optional<UserBankAccount> findByIdAndUserId(UUID id, UUID userId);

    // Tìm account đang là default của user
    // findBy + UserId + IsDefault → WHERE user_id = ? AND is_default = ?
    Optional<UserBankAccount> findByUserIdAndIsDefault(UUID userId, boolean isDefault);
}
