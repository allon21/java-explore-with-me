package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.enums.CommentState;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCommentUpdateDto {
    @NotBlank
    @Size(min = 1, max = 255)
    private String message;
    private CommentState state;
}
