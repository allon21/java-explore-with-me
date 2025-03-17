package ru.practicum.dto.comment;

import lombok.*;
import ru.practicum.dto.user.UserShortDto;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;

    private UserShortDto author;

    private String message;
}
