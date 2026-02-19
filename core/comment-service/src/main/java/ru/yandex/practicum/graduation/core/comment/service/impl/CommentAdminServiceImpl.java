package ru.yandex.practicum.graduation.core.comment.service.impl;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.graduation.core.comment.dto.request.SearchOfCommentByAdminDto;
import ru.yandex.practicum.graduation.core.comment.dto.response.CommentDto;
import ru.yandex.practicum.graduation.core.comment.mapper.CommentMapper;
import ru.yandex.practicum.graduation.core.comment.model.Comment;
import ru.yandex.practicum.graduation.core.comment.model.QComment;
import ru.yandex.practicum.graduation.core.comment.repository.CommentRepository;
import ru.yandex.practicum.graduation.core.comment.service.CommentAdminService;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
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
@Transactional(readOnly = true)
public class CommentAdminServiceImpl implements CommentAdminService {
    private final UserClient userClient;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Администратор удаляет комментарий с ID: {}", commentId);
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException(String.format("Comment with id=%s was not found", commentId));
        }
        commentRepository.deleteById(commentId);
        log.info("Комментарий с ID: {} удален", commentId);
    }

    @Override
    public List<CommentDto> getComments(SearchOfCommentByAdminDto searchDto, Pageable pageable) {
        log.info("Администратор получает комментарии с фильтрами: {}", searchDto);

        QComment comment = QComment.comment;
        BooleanBuilder predicate = new BooleanBuilder();

        if (searchDto.getUsers() != null && !searchDto.getUsers().isEmpty()) {
            predicate.and(comment.userId.in(searchDto.getUsers()));
        }
        if (searchDto.getEvents() != null && !searchDto.getEvents().isEmpty()) {
            predicate.and(comment.eventId.in(searchDto.getEvents()));
        }
        if (searchDto.getRangeStart() != null) {
            predicate.and(comment.createdOn.goe(searchDto.getRangeStart()));
        }
        if (searchDto.getRangeEnd() != null) {
            predicate.and(comment.createdOn.loe(searchDto.getRangeEnd()));
        }
        List<Comment> comments =  commentRepository.findAll(predicate, pageable).getContent();
        List<UserDto> users;
        try{
            users = userClient.findUsersByIds(comments.stream().map(Comment::getUserId).toList());
        } catch (Exception e){
            throw new UserClientException("не удалось получить данные от сервиса пользователей");
        }
        Map<Long, UserDto> userByIdMap = users.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return comments.stream().map(c -> commentMapper.toDto(c, userByIdMap.get(c.getUserId()))).toList();
    }
}