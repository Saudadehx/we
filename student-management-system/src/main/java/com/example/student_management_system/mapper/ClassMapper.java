package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Class;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ClassMapper {
    List<Class> findAllWithMajor();
    Class findById(Long id);
    List<Class> findByMajorId(Long majorId);
    void insert(Class newClass);
    int update(Class newClass);
    void deleteById(Long id);
}