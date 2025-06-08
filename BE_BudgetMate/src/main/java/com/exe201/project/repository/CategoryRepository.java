package com.exe201.project.repository;

import com.exe201.project.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Category c WHERE c.name = :name")
    List<Category> findByName(@Param("name") String name);
    
    boolean existsByName(String name);
}
