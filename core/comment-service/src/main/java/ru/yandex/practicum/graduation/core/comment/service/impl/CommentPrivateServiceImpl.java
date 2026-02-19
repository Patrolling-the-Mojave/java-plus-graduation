package ru.yandex.practicum.graduation.core.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.graduation.core.comment.dto.request.NewCommentDto;
import ru.yandex.practicum.graduation.core.comment.dto.response.CommentDto;
import ru.yandex.practicum.graduation.core.comment.mapper.CommentMapper;
import ru.yandex.practicum.graduation.core.comment.model.Comment;
import ru.yandex.practicum.graduation.core.comment.repository.CommentRepository;
import ru.yandex.practicum.graduation.core.comment.service.CommentPrivateService;
import ru.yandex.practicum.graduation.core.dto.event.EventDto;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;
import ru.yandex.practicum.graduation.core.interaction.EventClient;
import ru.yandex.practicum.graduation.core.interaction.EventClientException;
import ru.yandex.practicum.graduation.core.interaction.UserClient;
import ru.yandex.practicum.graduation.core.interaction.UserClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentPrivateServiceImpl implements CommentPrivateService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final CommentMapper commentMapper;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.info("Пользователь с ID: {} добавляет комментарий к событию с ID: {}", userId, eventId);
        UserDto user = getUserById(userId);

        EventDto event = getEventById(eventId);
        if (event == null) {
            throw new NotFoundException("пользователь с id" + userId + " не найден");
        }
        Comment comment = commentMapper.toEntity(newCommentDto);
        comment.setUserId(userId);
        comment.setEventId(eventId);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setUpdatedOn(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        log.info("Добавлен новый комментарий: {}", savedComment);
        return commentMapper.toDto(savedComment, user);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("Пользователь с ID: {} пытается удалить свой комментарий с ID: {}", userId, commentId);
        UserDto user = getUserById(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%s was not found", commentId)));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException(String.format("User with ID: %s is not the author of the comment with ID: %s", userId, commentId));
        }

        commentRepository.delete(comment);
        log.info("Комментарий с ID: {} удален", commentId);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto updateCommentDto) {
        log.info("Пользователь с ID: {} пытается обновить свой комментарий с ID: {}", userId, commentId);
        UserDto user = getUserById(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%s was not found", commentId)));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException(String.format("User with ID: %s is not the author of the comment with ID: %s", userId, commentId));
        }

        comment.setText(updateCommentDto.getText());
        comment.setUpdatedOn(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Комментарий с ID: {} обновлен", commentId);
        return commentMapper.toDto(updatedComment, user);
    }

    @Override
    public List<CommentDto> getCommentsByUserId(Long userId, Pageable pageable) {
        log.info("Получение комментариев пользователя с ID: {}", userId);
        UserDto userDto = getUserById(userId);

        return commentRepository.findByUserIdOrderByCreatedOnDesc(userId, pageable).stream()
                .map(comment -> commentMapper.toDto(comment, userDto))
                .collect(Collectors.toList());
    }

    private UserDto getUserById(Long userId) {
        try {
            return userClient.findUserById(userId);
        } catch (Exception e) {
            throw new UserClientException("не удалось получить данные от сервиса пользователей", e);
        }
    }

    private EventDto getEventById(Long eventId) {
        try {
            return eventClient.findEventById(eventId);
        } catch (Exception e) {
            throw new EventClientException("не удалось получить данные от сервиса ивентов", e);
        }
    }
}