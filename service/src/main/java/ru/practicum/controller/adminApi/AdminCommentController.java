package ru.practicum.controller.adminApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.AdminCommentUpdateDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/events/{eventId}/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable long eventId,
                                                        @RequestParam(name = "from", defaultValue = "0") int from,
                                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.getAllComments(eventId, from, size));
    }

    @DeleteMapping("/{comId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long eventId,
                                              @PathVariable Long comId) {
        commentService.adminDelete(eventId, comId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{comId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long eventId,
                                                    @PathVariable Long comId,
                                                    @RequestBody @Valid AdminCommentUpdateDto adminCommentUpdateDto) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.adminUpdate(eventId, comId, adminCommentUpdateDto));
    }
}
