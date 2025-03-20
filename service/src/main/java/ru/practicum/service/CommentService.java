package ru.practicum.service;

import ru.practicum.dto.comment.AdminCommentUpdateDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {
    void adminDelete(Long eventId, Long comId);

    CommentDto adminUpdate(Long eventId, Long comId, AdminCommentUpdateDto adminCommentUpdateDto);

    List<CommentDto> getAllComments(long eventId, int from, int size);

    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long eventId, Long comId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long eventId, Long comId);
}
