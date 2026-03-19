package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.CreateBankAccountRequest;
import com.sun.bookingtours.dto.request.UpdateBankAccountRequest;
import com.sun.bookingtours.dto.response.BankAccountResponse;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.entity.UserBankAccount;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.UserMapper;
import com.sun.bookingtours.repository.UserBankAccountRepository;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final UserBankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<BankAccountResponse> getAll(UserPrincipal principal) {
        // Stream + map: duyệt qua từng entity trong list, convert sang DTO rồi gom lại thành list mới
        return bankAccountRepository.findByUserId(principal.getId())
                .stream()
                .map(userMapper::toBankAccountResponse)
                .toList();
    }

    @Transactional
    public BankAccountResponse create(UserPrincipal principal, CreateBankAccountRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));

        UserBankAccount account = UserBankAccount.builder()
                .user(user)
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .isDefault(false)
                .build();

        return userMapper.toBankAccountResponse(bankAccountRepository.save(account));
    }

    @Transactional
    public BankAccountResponse update(UserPrincipal principal, UUID accountId, UpdateBankAccountRequest request) {
        UserBankAccount account = findAccountByIdAndUser(accountId, principal.getId());

        // Partial update: chỉ update field nào client gửi lên
        if (request.getBankName() != null) account.setBankName(request.getBankName());
        if (request.getAccountNumber() != null) account.setAccountNumber(request.getAccountNumber());
        if (request.getAccountHolder() != null) account.setAccountHolder(request.getAccountHolder());

        // Không cần save() — JPA dirty checking tự UPDATE khi transaction kết thúc
        return userMapper.toBankAccountResponse(account);
    }

    @Transactional
    public void delete(UserPrincipal principal, UUID accountId) {
        UserBankAccount account = findAccountByIdAndUser(accountId, principal.getId());
        bankAccountRepository.delete(account);
    }

    @Transactional
    public BankAccountResponse setDefault(UserPrincipal principal, UUID accountId) {
        // Tìm account cần set default (validate ownership luôn)
        UserBankAccount account = findAccountByIdAndUser(accountId, principal.getId());

        // Unset account đang là default hiện tại (nếu có)
        bankAccountRepository.findByUserIdAndIsDefault(principal.getId(), true)
                .ifPresent(current -> current.setDefault(false));  // dirty checking tự UPDATE

        account.setDefault(true);

        return userMapper.toBankAccountResponse(account);
    }

    // ---- private helpers ----

    private UserBankAccount findAccountByIdAndUser(UUID accountId, UUID userId) {
        // Tìm theo cả accountId + userId → đảm bảo user chỉ thao tác account của chính mình
        // Nếu accountId tồn tại nhưng không thuộc user → 404 (không lộ thông tin account người khác)
        return bankAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", accountId));
    }
}
