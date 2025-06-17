package com.example.student_management_system.service;

import com.example.student_management_system.dto.CourseDTO;
import com.example.student_management_system.dto.CourseResponseDTO;
import com.example.student_management_system.mapper.CourseMapper;
import com.example.student_management_system.mapper.TeacherMapper;
import com.example.student_management_system.model.Course;
import com.example.student_management_system.model.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getAllCoursesWithTeacherName(Map<String, Object> params) {
        List<Course> courses = courseMapper.findAll(params);
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        List<Teacher> teachers = teacherMapper.findAll();
        // 【修复】将 Teacher::itself 替换为 Function.identity()
        Map<Long, Teacher> teacherMap = teachers.stream()
                .collect(Collectors.toMap(Teacher::getId, Function.identity()));

        return courses.stream().map(course -> {
            CourseResponseDTO dto = new CourseResponseDTO();
            dto.setId(course.getId());
            dto.setCourseId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setCredits(course.getCredits());
            dto.setCourseDay(course.getCourseDay());
            dto.setCourseTime(course.getCourseTime());

            Teacher teacher = teacherMap.get(course.getTeacherId());
            dto.setTeacherName(teacher != null ? teacher.getName() : "未知教师");
            dto.setTeacherId(teacher != null ? teacher.getTeacherId() : null);
            return dto;
        }).collect(Collectors.toList());
    }
    public Course createCourse(CourseDTO courseDTO) {
        Teacher teacher = teacherMapper.findByTeacherId(courseDTO.getTeacherId());
        if (teacher == null) {
            throw new ResourceNotFoundException("工号为 " + courseDTO.getTeacherId() + " 的教师不存在。");
        }

        Course course = new Course();
        course.setCourseId(courseDTO.getCourseId());
        course.setCourseName(courseDTO.getCourseName());
        course.setCredits(courseDTO.getCredits());
        course.setTeacherId(teacher.getId());
        course.setCourseDay(courseDTO.getCourseDay());
        course.setCourseTime(courseDTO.getCourseTime());

        courseMapper.insert(course);
        return course;
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> findCoursesByTeacherId(Long teacherId) {
        List<Course> courses = courseMapper.findByTeacherId(teacherId);
        Teacher teacher = teacherMapper.findById(teacherId); // 修正：应通过ID获取教师

        return courses.stream().map(course -> {
            CourseResponseDTO dto = new CourseResponseDTO();
            dto.setId(course.getId());
            dto.setCourseId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setCredits(course.getCredits());
            if (teacher != null) {
                dto.setTeacherName(teacher.getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public Course updateCourse(Long courseId, CourseDTO courseDTO) {
        Course existingCourse = courseMapper.findById(courseId);
        if (existingCourse == null) {
            throw new ResourceNotFoundException("ID为 " + courseId + " 的课程不存在。");
        }

        Teacher teacher = teacherMapper.findByTeacherId(courseDTO.getTeacherId());
        if (teacher == null) {
            throw new ResourceNotFoundException("工号为 " + courseDTO.getTeacherId() + " 的教师不存在。");
        }

        existingCourse.setCourseId(courseDTO.getCourseId());
        existingCourse.setCourseName(courseDTO.getCourseName());
        existingCourse.setCredits(courseDTO.getCredits());
        existingCourse.setTeacherId(teacher.getId());
        existingCourse.setCourseDay(courseDTO.getCourseDay());
        existingCourse.setCourseTime(courseDTO.getCourseTime());

        courseMapper.update(existingCourse);
        return existingCourse;
    }

    public void deleteCourse(Long id) {
        if (courseMapper.findById(id) == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的课程不存在，无法删除。");
        }
        courseMapper.deleteById(id);
    }
}