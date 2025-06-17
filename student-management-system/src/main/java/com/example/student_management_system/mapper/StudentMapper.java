package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Student;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map; // 导入 Map

@Mapper
public interface StudentMapper {
    // 【修改】让 findAll 方法接收一个Map类型的参数
    List<Student> findAll(Map<String, Object> params);
    Student findById(Long id);
    List<Student> findByIds(List<Long> ids);
    Student findByStudentId(String studentId);
    void insert(Student student);
    int update(Student student);
    void deleteById(Long id);
}