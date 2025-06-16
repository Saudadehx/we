package com.example.student_management_system.service;

import com.example.student_management_system.dto.DashboardStatsDTO;
import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.model.Student;
// import com.example.student_management_system.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {


    private final StudentMapper studentMapper;

    @Autowired
    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }



    public DashboardStatsDTO getDashboardStats() {
        // 修改点4: 使用 studentMapper 获取数据
        List<Student> students = studentMapper.findAll();
        long totalStudents = students.size();
        long totalClasses = students.stream().map(Student::getClassName).distinct().count();
        long totalMajors = students.stream().map(Student::getMajor).filter(m -> m != null && !m.isEmpty()).distinct().count();
        long totalGrades = students.stream()
                .map(s -> s.getClassName().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(0, 2))
                .distinct()
                .count();

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalStudents(totalStudents);
        stats.setTotalClasses(totalClasses);
        stats.setTotalMajors(totalMajors);
        stats.setTotalGrades(totalGrades);
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentMapper.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(Long id) {
        // MyBatis 返回 null, 我们将其包装成 Optional 以适应 Controller
        return Optional.ofNullable(studentMapper.findById(id));
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentByStudentId(String studentId) {
        return Optional.ofNullable(studentMapper.findByStudentId(studentId));
    }

    @Transactional
    public Student createStudent(Student student) {
        // 检查学号是否已存在
        Student existingStudent = studentMapper.findByStudentId(student.getStudentId());
        if (existingStudent != null) {
            throw new IllegalArgumentException("学号 " + student.getStudentId() + " 已存在。");
        }
        studentMapper.insert(student);
        return student;
    }

    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        // 检查要更新的学生是否存在
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生。");
        }

        // 检查更新的学号是否与系统中其他学生冲突
        if (studentDetails.getStudentId() != null && !studentDetails.getStudentId().equals(student.getStudentId())) {
            Student studentWithNewStudentId = studentMapper.findByStudentId(studentDetails.getStudentId());
            if (studentWithNewStudentId != null) {
                throw new IllegalArgumentException("学号 " + studentDetails.getStudentId() + " 已被其他学生使用。");
            }
        }

        // 确保要更新的实体ID正确
        studentDetails.setId(id);
        studentMapper.update(studentDetails);
        return studentDetails;
    }

    @Transactional
    public void deleteStudent(Long id) {
        // 检查要删除的学生是否存在
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生，无法删除。");
        }
        studentMapper.deleteById(id);
    }
}