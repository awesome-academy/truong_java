package com.sun.bookingtours.service;

import com.sun.bookingtours.dto.request.NewsRequest;
import com.sun.bookingtours.dto.response.NewsResponse;
import com.sun.bookingtours.entity.News;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.exception.BusinessException;
import com.sun.bookingtours.exception.ResourceNotFoundException;
import com.sun.bookingtours.mapper.NewsMapper;
import com.sun.bookingtours.repository.NewsRepository;
import com.sun.bookingtours.repository.UserRepository;
import com.sun.bookingtours.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NewsMapper newsMapper;

    // Public: chỉ trả bài đã published
    public Page<NewsResponse> listPublished(Pageable pageable) {
        return newsRepository.findAllByIsPublishedTrue(pageable)
                .map(newsMapper::toResponse);
    }

    public NewsResponse getBySlug(String slug) {
        News news = newsRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("News", slug));
        return newsMapper.toResponse(news);
    }

    @Transactional
    public NewsResponse create(UserPrincipal principal, NewsRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getTitle());

        if (newsRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));

        News news = News.builder()
                .author(author)
                .title(request.getTitle())
                .slug(slug)
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        return newsMapper.toResponse(newsRepository.save(news));
    }

    @Transactional
    public NewsResponse update(UUID id, NewsRequest request) {
        News news = findById(id);
        String slug = resolveSlug(request.getSlug(), request.getTitle());

        if (!slug.equals(news.getSlug()) && newsRepository.existsBySlug(slug)) {
            throw new BusinessException("Slug '" + slug + "' đã tồn tại");
        }

        news.setTitle(request.getTitle());
        news.setSlug(slug);
        news.setContent(request.getContent());
        news.setThumbnailUrl(request.getThumbnailUrl());

        return newsMapper.toResponse(news);
    }

    @Transactional
    public void delete(UUID id) {
        newsRepository.delete(findById(id));
    }

    @Transactional
    public NewsResponse publish(UUID id) {
        News news = findById(id);

        if (news.isPublished()) {
            throw new BusinessException("Bài viết đã được publish");
        }

        news.setPublished(true);
        news.setPublishedAt(LocalDateTime.now());

        return newsMapper.toResponse(news);
    }

    private News findById(UUID id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", id));
    }

    private String resolveSlug(String requestSlug, String title) {
        if (requestSlug != null && !requestSlug.isBlank()) {
            return requestSlug.trim().toLowerCase();
        }
        return generateSlug(title);
    }

    private String generateSlug(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d").replaceAll("Đ", "D")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
