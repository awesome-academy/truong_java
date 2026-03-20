package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.UserBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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

    // Unset tất cả default của user trong 1 câu UPDATE
    // Dùng trước khi set default mới → tránh race condition nhiều row cùng is_default = true
    @Modifying
    @Query("UPDATE UserBankAccount a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultByUserId(UUID userId);
}
