package ru.practicum.controller.privateApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> addComment(@PathVariable Long userId,
                                                 @PathVariable Long eventId,
                                                 @RequestBody @Valid NewCommentDto newCommentDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(userId, eventId, newCommentDto));
    }

    @PatchMapping("/{comId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @PathVariable Long comId,
                                                    @RequestBody @Valid NewCommentDto newCommentDto) {

        return ResponseEntity.status(HttpStatus.OK).body(commentService.updateComment(userId, eventId, comId, newCommentDto));
    }

    @DeleteMapping("/{comId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long userId,
                                              @PathVariable Long eventId,
                                              @PathVariable Long comId) {
        commentService.deleteComment(userId, eventId, comId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

