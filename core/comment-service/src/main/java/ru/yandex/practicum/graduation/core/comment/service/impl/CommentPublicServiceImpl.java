package ru.yandex.practicum.graduation.core.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.graduation.core.comment.dto.response.CommentDto;
import ru.yandex.practicum.graduation.core.comment.mapper.CommentMapper;
import ru.yandex.practicum.graduation.core.comment.model.Comment;
import ru.yandex.practicum.graduation.core.comment.repository.CommentRepository;
import ru.yandex.practicum.graduation.core.comment.service.CommentPublicService;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.interaction.UserClient;
import ru.yandex.practicum.graduation.core.interaction.UserClientException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPublicServiceImpl implements CommentPublicService {

    private final UserClient userClient;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Override
    public List<CommentDto> getCommentsByEventId(Long eventId, Pageable pageable) {
        log.info("Получение комментариев для события с ID: {}", eventId);
        List<Comment> comments = commentRepository.findByEventIdOrderByCreatedOnDesc(eventId, pageable);
        List<UserDto> users;
        try {
            users = userClient.findUsersByIds(comments.stream().map(Comment::getUserId).toList());
        } catch (Exception e) {
            throw new UserClientException("не удалось получить данные от сервиса пользователей");
        }
        Map<Long, UserDto> userByIdMap = users.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return comments.stream()
                .map(comment ->
                        commentMapper.toDto(comment, userByIdMap.get(comment.getUserId())))
                .toList();
    }
}