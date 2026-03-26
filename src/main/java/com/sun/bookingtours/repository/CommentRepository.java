package com.sun.bookingtours.repository;

import com.sun.bookingtours.entity.Comment;
import com.sun.bookingtours.entity.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // JOIN FETCH c.user → load user cùng 1 query, tránh N+1 khi map fullName/avatarUrl
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.targetType = :targetType AND c.targetId = :targetId")
    List<Comment> findByTargetTypeAndTargetId(
            @Param("targetType") TargetType targetType,
            @Param("targetId") UUID targetId
    );

    Optional<Comment> findByIdAndUserId(UUID id, UUID userId);
}
