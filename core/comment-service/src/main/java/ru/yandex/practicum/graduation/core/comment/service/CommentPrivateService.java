package ru.yandex.practicum.graduation.core.comment.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.graduation.core.comment.dto.request.NewCommentDto;
import ru.yandex.practicum.graduation.core.comment.dto.response.CommentDto;


import java.util.List;

public interface CommentPrivateService {
    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long commentId);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto updateCommentDto);

    List<CommentDto> getCommentsByUserId(Long userId, Pageable pageable);
}
