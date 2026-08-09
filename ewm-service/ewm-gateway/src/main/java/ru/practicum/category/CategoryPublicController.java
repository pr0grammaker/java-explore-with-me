package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.http.client.CategoryHttpPublicClient;

import java.util.Collection;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryHttpPublicClient categoryHttpPublicClient;

    @GetMapping
    public ResponseEntity<Collection<CategoryDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(categoryHttpPublicClient.getAllCategories(from, size).getBody());
    }

    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long catId) {
        return ResponseEntity.ok().body(categoryHttpPublicClient.getCategoryById(catId).getBody());
    }
}
