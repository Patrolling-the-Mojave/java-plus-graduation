package ru.yandex.practicum.graduation.core.comment.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.graduation.core.comment.dto.response.CommentDto;


import java.util.List;

public interface CommentPublicService {
    List<CommentDto> getCommentsByEventId(Long eventId, Pageable pageable);
}
