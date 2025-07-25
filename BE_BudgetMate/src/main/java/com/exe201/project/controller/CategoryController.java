package com.exe201.project.controller;

import com.exe201.project.dto.request.BulkCategoryRequest;
import com.exe201.project.dto.request.CategoryRequest;
import com.exe201.project.dto.request.WalletRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.CategoryResponse;
import com.exe201.project.dto.response.WalletResponse;
import com.exe201.project.entity.Category;
import com.exe201.project.service.ICategoryService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/category")
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryService categoryService;

//    @PostMapping()
//    @ResponseStatus(HttpStatus.CREATED)
//    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) throws MessagingException {
//        CategoryResponse category = categoryService.addCategory(request);
//        return ResponseEntity.ok(
//                ApiResponse.<CategoryResponse>builder()
//                        .message("Category data created successfully.")
//                        .data(category)
//                        .build()
//        );
//    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> createMultipleCategories(@Valid @RequestBody BulkCategoryRequest request) {
        List<CategoryResponse> categories = categoryService.addMultipleCategories(request);
        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponse>>builder()
                        .message("Categories created successfully. Total created: " + categories.size())
                        .data(categories)
                        .build()
        );
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<?>> getAllCategory() {
        List<CategoryResponse> category = categoryService.getAllCategories();
        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponse>>builder()
                        .message("Category data retrieved successfully.")
                        .data(category)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{cateId}")
    public ResponseEntity<ApiResponse<?>> updateCategory(
            @PathVariable long cateId,
            @Valid @RequestBody CategoryRequest request
    ) throws MessagingException {
        CategoryResponse category = categoryService.updateCategory(request, cateId);
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Category data updated successfully.")
                        .data(category)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{cateId}")
    public ResponseEntity<ApiResponse<?>> deleteCategory(
            @PathVariable long cateId
    ) {
        categoryService.deleteCategory(cateId);
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Category data deleted successfully.")
                        .build()
        );
    }
}
