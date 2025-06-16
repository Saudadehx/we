package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Student;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface StudentMapper {
    List<Student> findAll();
    Student findById(Long id);
    List<Student> findByIds(List<Long> ids);
    Student findByStudentId(String studentId);
    void insert(Student student);
    int update(Student student);
    void deleteById(Long id);
}