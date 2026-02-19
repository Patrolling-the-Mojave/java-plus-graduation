package ru.yandex.practicum.graduation.core.comment.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.graduation.core.comment.dto.request.SearchOfCommentByAdminDto;
import ru.yandex.practicum.graduation.core.comment.dto.response.CommentDto;


import java.util.List;

public interface CommentAdminService {
    void deleteComment(Long commentId);

    List<CommentDto> getComments(SearchOfCommentByAdminDto searchDto, Pageable pageable);
}
