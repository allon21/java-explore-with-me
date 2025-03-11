package ru.practicum.dto.category;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;

    public CategoryDto(Long id) {
        this.id = id;
    }
}
