package com.example.student_management_system.service;

import com.example.student_management_system.dto.CourseDTO;
import com.example.student_management_system.dto.CourseResponseDTO;
import com.example.student_management_system.mapper.CourseMapper;
import com.example.student_management_system.mapper.MajorMapper;
import com.example.student_management_system.mapper.TeacherMapper;
import com.example.student_management_system.model.Course;
import com.example.student_management_system.model.Major;
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
    @Autowired
    private MajorMapper majorMapper; // 注入MajorMapper

    private CourseResponseDTO convertToResponseDTO(Course course, Map<Long, Teacher> teacherMap, Map<Long, Major> majorMap) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setId(course.getId());
        dto.setCourseId(course.getCourseId());
        dto.setCourseName(course.getCourseName());
        dto.setCredits(course.getCredits());
        dto.setCourseDay(course.getCourseDay());
        dto.setCourseTime(course.getCourseTime());
        dto.setCourseType(course.getCourseType());
        dto.setMajorId(course.getMajorId());
        dto.setAcademicYear(course.getAcademicYear());
        dto.setSemester(course.getSemester());

        Teacher teacher = teacherMap.get(course.getTeacherId());
        dto.setTeacherName(teacher != null ? teacher.getName() : "未知教师");
        dto.setTeacherId(teacher != null ? teacher.getTeacherId() : null);

        Major major = majorMap.get(course.getMajorId());
        dto.setMajorName(major != null ? major.getName() : null);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getAllCoursesWithTeacherName(Map<String, Object> params) {
        List<Course> courses = courseMapper.findAll(params);
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        List<Teacher> teachers = teacherMapper.findAll();
        Map<Long, Teacher> teacherMap = teachers.stream()
                .collect(Collectors.toMap(Teacher::getId, Function.identity()));

        List<Major> majors = majorMapper.findAll();
        Map<Long, Major> majorMap = majors.stream()
                .collect(Collectors.toMap(Major::getId, Function.identity()));

        return courses.stream()
                .map(course -> convertToResponseDTO(course, teacherMap, majorMap))
                .collect(Collectors.toList());
    }

    public Course createCourse(CourseDTO courseDTO) {
        Teacher teacher = teacherMapper.findByTeacherId(courseDTO.getTeacherId());
        if (teacher == null) {
            throw new ResourceNotFoundException("工号为 " + courseDTO.getTeacherId() + " 的教师不存在。");
        }
        if ("COMPULSORY".equals(courseDTO.getCourseType()) && courseDTO.getMajorId() == null) {
            throw new IllegalArgumentException("必修课必须指定一个专业。");
        }

        Course course = new Course();
        course.setCourseId(courseDTO.getCourseId());
        course.setCourseName(courseDTO.getCourseName());
        course.setCredits(courseDTO.getCredits());
        course.setTeacherId(teacher.getId());
        course.setCourseDay(courseDTO.getCourseDay());
        course.setCourseTime(courseDTO.getCourseTime());
        // 设置新字段
        course.setCourseType(courseDTO.getCourseType());
        course.setMajorId(courseDTO.getMajorId());
        course.setAcademicYear(courseDTO.getAcademicYear());
        course.setSemester(courseDTO.getSemester());


        courseMapper.insert(course);
        return course;
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> findCoursesByTeacherId(Long teacherId) {
        List<Course> courses = courseMapper.findByTeacherId(teacherId);
        Teacher teacher = teacherMapper.findById(teacherId);
        // 为了转换DTO，我们需要所有的major信息
        List<Major> majors = majorMapper.findAll();
        Map<Long, Major> majorMap = majors.stream().collect(Collectors.toMap(Major::getId, Function.identity()));
        Map<Long, Teacher> teacherMap = Map.of(teacherId, teacher);

        return courses.stream()
                .map(course -> convertToResponseDTO(course, teacherMap, majorMap))
                .collect(Collectors.toList());
    }

    public Course updateCourse(Long courseDbId, CourseDTO courseDTO) {
        Course existingCourse = courseMapper.findById(courseDbId);
        if (existingCourse == null) {
            throw new ResourceNotFoundException("ID为 " + courseDbId + " 的课程不存在。");
        }

        Teacher teacher = teacherMapper.findByTeacherId(courseDTO.getTeacherId());
        if (teacher == null) {
            throw new ResourceNotFoundException("工号为 " + courseDTO.getTeacherId() + " 的教师不存在。");
        }
        if ("COMPULSORY".equals(courseDTO.getCourseType()) && courseDTO.getMajorId() == null) {
            throw new IllegalArgumentException("必修课必须指定一个专业。");
        }


        existingCourse.setCourseId(courseDTO.getCourseId());
        existingCourse.setCourseName(courseDTO.getCourseName());
        existingCourse.setCredits(courseDTO.getCredits());
        existingCourse.setTeacherId(teacher.getId());
        existingCourse.setCourseDay(courseDTO.getCourseDay());
        existingCourse.setCourseTime(courseDTO.getCourseTime());
        // 更新新字段
        existingCourse.setCourseType(courseDTO.getCourseType());
        existingCourse.setMajorId(courseDTO.getMajorId());
        existingCourse.setAcademicYear(courseDTO.getAcademicYear());
        existingCourse.setSemester(courseDTO.getSemester());

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