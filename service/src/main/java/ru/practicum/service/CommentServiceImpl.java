package ru.practicum.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.enums.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    public final UserRepository userRepository;
    public final EventRepository eventRepository;
    public final CommentRepository commentRepository;
    public final CommentMapper commentMapper;

    @Override
    @Transactional
    public void adminDelete(Long eventId, Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Comment не найден"));
        if (!((comment.getEvent().getId()) == eventId)) {
            throw new ConflictException("Comment не относиться к этому событию");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentDto adminUpdate(Long eventId, Long comId, NewCommentDto newCommentDto) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Comment не найден"));
        if (!((comment.getEvent().getId()) == eventId)) {
            throw new ConflictException("Comment не относиться к этому событию");
        }
        comment.setMessage(newCommentDto.getMessage());
        return commentMapper.getCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getAllComments(long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.ASC, "id"));
        return commentRepository.findAllByEventId(pageable, eventId)
                .stream()
                .map(commentMapper::getCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event не найден"));
        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Event не опубликован");
        }

        Comment comment = commentMapper.getComment(newCommentDto);
        comment.setEvent(event);
        comment.setAuthor(author);
        comment = commentRepository.save(comment);

        return commentMapper.getCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long comId, NewCommentDto newCommentDto) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Comment не найден"));
        if (!((comment.getEvent().getId()) == eventId)) {
            throw new ConflictException("Comment не относиться к этому событию");
        }
        if (!((comment.getAuthor().getId()) == userId)) {
            throw new ConflictException("User не является автором комментария");
        }

        comment.setMessage(newCommentDto.getMessage());
        return commentMapper.getCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long eventId, Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Comment не найден"));
        if (!((comment.getEvent().getId()) == eventId)) {
            throw new ConflictException("Comment не относиться к этому событию");
        }
        if (!((comment.getAuthor().getId()) == userId)) {
            throw new ConflictException("User не является автором комментария");
        }
        commentRepository.delete(comment);
    }
}
