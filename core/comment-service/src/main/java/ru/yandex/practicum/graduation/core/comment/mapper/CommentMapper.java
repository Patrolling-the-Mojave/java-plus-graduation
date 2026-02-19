package ru.yandex.practicum.graduation.core.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.graduation.core.comment.dto.request.NewCommentDto;
import ru.yandex.practicum.graduation.core.comment.dto.response.CommentDto;
import ru.yandex.practicum.graduation.core.comment.model.Comment;
import ru.yandex.practicum.graduation.core.dto.user.UserDto;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    Comment toEntity(NewCommentDto newCommentDto);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", source = "comment.id")
    CommentDto toDto(Comment comment, UserDto user);
}
