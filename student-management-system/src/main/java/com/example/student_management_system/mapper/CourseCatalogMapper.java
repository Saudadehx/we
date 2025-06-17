package com.example.student_management_system.mapper;

import com.example.student_management_system.model.CourseCatalog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CourseCatalogMapper {
    CourseCatalog findById(Long id);
    CourseCatalog findByCourseCode(String courseCode);
    List<CourseCatalog> findAll();
    void insert(CourseCatalog courseCatalog);
    int update(CourseCatalog courseCatalog);
    void deleteById(Long id);
}