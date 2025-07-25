package com.exe201.project.service.impl;

import com.exe201.project.dto.request.BulkCategoryRequest;
import com.exe201.project.dto.request.CategoryRequest;
import com.exe201.project.dto.response.CategoryResponse;
import com.exe201.project.entity.Category;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.exception.ResourceAlreadyExistException;
import com.exe201.project.mapper.CategoryMapper;
import com.exe201.project.repository.CategoryRepository;
import com.exe201.project.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse addCategory(CategoryRequest request) {
        Category category = new Category();

        category.setName(request.name());
        categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(CategoryRequest request, long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(request.name());
        categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public void deleteCategory(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setDeleted(true);
    }

    @Override
    @Transactional
    public List<CategoryResponse> addMultipleCategories(BulkCategoryRequest request) {
        // Check for duplicate names within the request
        List<String> categoryNames = request.categories().stream()
                .map(CategoryRequest::name)
                .collect(Collectors.toList());
        
        Set<String> uniqueNames = new HashSet<>(categoryNames);
        if (uniqueNames.size() != categoryNames.size()) {
            throw new IllegalArgumentException("Duplicate category names found in request");
        }
        
        // Check for existing categories with the same names
        List<String> existingNames = categoryRepository.findExistingNamesByNames(categoryNames);
        if (!existingNames.isEmpty()) {
            throw new ResourceAlreadyExistException("Categories with names already exist: " + String.join(", ", existingNames));
        }
        
        // Create and save all categories
        List<Category> categoriesToSave = request.categories().stream()
                .map(categoryRequest -> {
                    Category category = new Category();
                    category.setName(categoryRequest.name());
                    category.setColor(categoryRequest.color());
                    return category;
                })
                .collect(Collectors.toList());
        
        List<Category> savedCategories = categoryRepository.saveAll(categoriesToSave);
        
        return savedCategories.stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }
}
