package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsHitDto {
    private Long id;
    @NotBlank(message = "APP не может быть пустым")
    private String app;
    @NotBlank(message = "URL не может быть пустым")
    private String uri;
    @NotBlank(message = "IP не может быть пустым")
    @Pattern(regexp = "([0-9]{1,3}[.]){3}[0-9]{1,3}", message = "Некорректный формат IP-адреса")
    private String ip;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
