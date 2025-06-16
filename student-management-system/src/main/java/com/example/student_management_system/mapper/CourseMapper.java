package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Course;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CourseMapper {
    Course findById(Long id);
    Course findByCourseId(String courseId);
    List<Course> findByIds(List<Long> ids); // <-- 添加这一行
    List<Course> findAll();
    List<Course> findByTeacherId(Long teacherId);
    void insert(Course course);
    int update(Course course);
    void deleteById(Long id);
}