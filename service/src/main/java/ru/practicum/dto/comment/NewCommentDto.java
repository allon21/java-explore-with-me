package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {
    @NotBlank
    @Size(min = 1, max = 255)
    private String message;
}
