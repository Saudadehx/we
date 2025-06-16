package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Student;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StudentMapper {
    List<Student> findAll();
    Student findById(Long id);
    Student findByStudentId(String studentId);
    void insert(Student student);
    void update(Student student);
    void deleteById(Long id);
}