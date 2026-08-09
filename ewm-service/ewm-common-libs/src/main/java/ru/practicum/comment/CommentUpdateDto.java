package ru.practicum.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateDto {

    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 3, max = 2000, message = "Длина комментария должна быть от 3 до 2000 символов")
    private String text;
}
