package com.exe201.project.service;

import com.exe201.project.dto.request.BulkCategoryRequest;
import com.exe201.project.dto.request.CategoryRequest;
import com.exe201.project.dto.response.BulkCategoryResponse;
import com.exe201.project.dto.response.CategoryResponse;
import com.exe201.project.entity.Category;

import java.util.List;

public interface ICategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse addCategory(CategoryRequest request);
    List<CategoryResponse> addMultipleCategories(BulkCategoryRequest request);
    CategoryResponse updateCategory(CategoryRequest request, long id);
    void deleteCategory(long id);

}
