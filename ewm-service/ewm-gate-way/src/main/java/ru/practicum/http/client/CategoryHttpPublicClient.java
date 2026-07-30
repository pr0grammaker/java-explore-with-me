package ru.practicum.http.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import ru.practicum.category.CategoryDto;

import java.util.Collection;

@HttpExchange(
        accept = "application/json",
        contentType = "application/json",
        url = "/categories"
)
public interface CategoryHttpPublicClient {

    @GetExchange
    ResponseEntity<Collection<CategoryDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetExchange("/{catId}")
    ResponseEntity<CategoryDto> getCategoryById(@PathVariable("catId") Long catId);
}
