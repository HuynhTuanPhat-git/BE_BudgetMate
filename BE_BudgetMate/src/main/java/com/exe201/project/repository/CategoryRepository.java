package com.exe201.project.repository;

import com.exe201.project.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByNameContainingIgnoreCase(String name);

    List<Category> findAllByName(String name);

    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT c.name FROM Category c WHERE c.name IN :names")
    List<String> findExistingNamesByNames(@Param("names") List<String> names);
}
