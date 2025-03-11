package ru.practicum.dto.event;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    @NotNull
    @DecimalMin(value = "-90", inclusive = false)
    @DecimalMax(value = "90", inclusive = false)
    Double lat;

    @NotNull
    @DecimalMin(value = "-180", inclusive = false)
    @DecimalMax(value = "180", inclusive = false)
    Double lon;
}
