package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Course;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper {
    Course findById(Long id);
    Course findByCourseId(String courseId);
    List<Course> findByIds(List<Long> ids);
    // 【修改】findAll方法以接收Map类型的搜索参数
    List<Course> findAll(Map<String, Object> params);
    List<Course> findByTeacherId(Long teacherId);
    void insert(Course course);
    int update(Course course);
    void deleteById(Long id);
}