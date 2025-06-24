package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {
    List<Student> findAll(Map<String, Object> params);// 查询所有学生
    Student findById(Long id);// 根据ID查询学生
    List<Student> findByIds(List<Long> ids);// 根据ID列表查询学生
    Student findByStudentId(String studentId);// 根据学号查询学生
    void insert(Student student);// 插入新学生
    int update(Student student);// 更新学生信息
    void deleteById(Long id);// 根据ID删除学生
    List<Student> findByMajorAndAcademicInfo(@Param("majorId") Long majorId, @Param("academicYear") Integer academicYear, @Param("semester") Integer semester);// 根据专业和学年学期查询学生
    List<Student> findByClassAndAcademicInfo(@Param("classId") Long classId, @Param("academicYear") Integer academicYear, @Param("semester") Integer semester);// 根据班级和学年学期查询学生
}