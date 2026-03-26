package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.CreateCommentRequest;
import com.sun.bookingtours.dto.response.CommentResponse;
import com.sun.bookingtours.entity.Comment;
import com.sun.bookingtours.entity.enums.TargetType;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.repository.CommentRepository;
import com.sun.bookingtours.repository.NewsRepository;
import com.sun.bookingtours.repository.ReviewRepository;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final NewsRepository newsRepository;

    @Transactional
    public CommentResponse createComment(UserPrincipal principal, CreateCommentRequest request) {
        if (request.targetType() != TargetType.REVIEW && request.targetType() != TargetType.NEWS) {
            throw new BusinessException("targetType phải là REVIEW hoặc NEWS");
        }

        // Validate targetId tồn tại
        if (request.targetType() == TargetType.REVIEW
                && !reviewRepository.existsById(request.targetId())) {
            throw new ResourceNotFoundException("Review", request.targetId());
        }
        if (request.targetType() == TargetType.NEWS
                && !newsRepository.existsById(request.targetId())) {
            throw new ResourceNotFoundException("News", request.targetId());
        }

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", request.parentId()));

            // Đảm bảo reply đúng target, tránh reply chéo sang target khác
            if (parent.getTargetType() != request.targetType()
                    || !parent.getTargetId().equals(request.targetId())) {
                throw new BusinessException("parentId không thuộc target này");
            }

            // Chỉ hỗ trợ reply 1 cấp — nếu parent đã là reply thì từ chối
            if (parent.getParent() != null) {
                throw new BusinessException("Chỉ hỗ trợ reply 1 cấp");
            }
        }

        Comment comment = Comment.builder()
                .user(userRepository.getReferenceById(principal.getId()))
                .targetType(request.targetType())
                .targetId(request.targetId())
                .parent(parent)
                .content(request.content())
                .build();

        Comment saved = commentRepository.save(comment);
        return toResponse(saved, Collections.emptyList());
    }

    @Transactional
    public void deleteComment(UserPrincipal principal, UUID commentId) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        // Replies tự bị xóa nhờ ON DELETE CASCADE trên parent_id trong DB
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(TargetType targetType, UUID targetId) {
        List<Comment> all = commentRepository.findByTargetTypeAndTargetId(targetType, targetId);

        // Group replies theo parent_id — tất cả fetch trong 1 query, build tree trong Java
        Map<UUID, List<Comment>> repliesByParentId = all.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        return all.stream()
                .filter(c -> c.getParent() == null)
                .map(c -> toResponse(c, repliesByParentId.getOrDefault(c.getId(), Collections.emptyList())))
                .toList();
    }

    private CommentResponse toResponse(Comment comment, List<Comment> replies) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getFullName(),
                comment.getUser().getAvatarUrl(),
                comment.getContent(),
                comment.getCreatedAt(),
                replies.stream().map(r -> toResponse(r, Collections.emptyList())).toList()
        );
    }
}
