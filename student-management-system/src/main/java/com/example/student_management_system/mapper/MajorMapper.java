package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Major;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MajorMapper {
    List<Major> findAll();
    Major findById(Long id);
    void insert(Major major);
    int update(Major major);
    void deleteById(Long id);
    Major findByName(String name);
}