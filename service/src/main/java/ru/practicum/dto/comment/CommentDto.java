package ru.practicum.dto.comment;

import lombok.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.CommentState;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;

    private UserShortDto author;

    private String message;

    private CommentState state;
}
