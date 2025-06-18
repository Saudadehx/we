package com.example.student_management_system.mapper;

import com.example.student_management_system.dto.TeacherDetailDTO; // ✨ 导入 DTO
import com.example.student_management_system.model.Teacher;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface TeacherMapper {

    Teacher findByTeacherId(String teacherId);

    List<Teacher> findAll();

    // ✨ 新增：定义获取教师及其课程列表的方法
    List<TeacherDetailDTO> findAllWithCourseNames();

    Teacher findById(Long id);

    List<Teacher> findByIds(List<Long> ids);

    void insert(Teacher teacher);

    int update(Teacher teacher);

    void deleteById(Long id);
}