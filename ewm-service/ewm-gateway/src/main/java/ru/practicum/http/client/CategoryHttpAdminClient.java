package ru.practicum.http.client;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import ru.practicum.category.CategoryDto;
import ru.practicum.category.NewCategoryDto;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/admin/categories"
)
public interface CategoryHttpAdminClient {

    @PostExchange
    ResponseEntity<CategoryDto> addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto);

    @DeleteExchange("{catId}")
    void deleteCategory(@PathVariable Long catId);

    @PatchExchange("{catId}")
    ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody NewCategoryDto newCategoryDto
    );
}
