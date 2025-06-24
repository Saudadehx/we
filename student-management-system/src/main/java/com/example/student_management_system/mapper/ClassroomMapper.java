package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Classroom;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ClassroomMapper {
    List<Classroom> findAll();
    Classroom findById(Long id);
    void insert(Classroom classroom);
    int update(Classroom classroom);
    void deleteById(Long id);
}