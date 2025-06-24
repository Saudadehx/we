package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {
    List<Student> findAll(Map<String, Object> params);
    Student findById(Long id);
    List<Student> findByIds(List<Long> ids);
    Student findByStudentId(String studentId);
    void insert(Student student);
    int update(Student student);
    void deleteById(Long id);
    List<Student> findByMajorAndAcademicInfo(@Param("majorId") Long majorId, @Param("academicYear") Integer academicYear, @Param("semester") Integer semester);
    List<Student> findByClassAndAcademicInfo(@Param("classId") Long classId, @Param("academicYear") Integer academicYear, @Param("semester") Integer semester);
}