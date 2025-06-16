package com.example.student_management_system.service;

import com.example.student_management_system.model.Student;
import com.example.student_management_system.repository.StudentRepository;
// 导入 jakarta.validation.Valid 和相关注解，如果需要在Service层触发校验的话
// 通常校验注解在Entity上，Controller层用@Valid触发
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 用于声明事务性方法
import com.example.student_management_system.dto.DashboardStatsDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service // 声明这是一个Service Bean
public class StudentService {
    public DashboardStatsDTO getDashboardStats() {
        List<Student> students = studentRepository.findAll();
        long totalStudents = students.size();
        long totalClasses = students.stream().map(Student::getClassName).distinct().count();
        long totalMajors = students.stream().map(Student::getMajor).filter(m -> m != null && !m.isEmpty()).distinct().count();
        long totalGrades = students.stream()
                .map(s -> s.getClassName().replaceAll("[^0-9]", "")) // 提取班级中的数字
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(0, 2)) // 取前两位作为年级
                .distinct()
                .count();

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalStudents(totalStudents);
        stats.setTotalClasses(totalClasses);
        stats.setTotalMajors(totalMajors);
        stats.setTotalGrades(totalGrades);
        return stats;
    }
    private final StudentRepository studentRepository;

    @Autowired // 构造函数注入 StudentRepository
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true) // 声明为只读事务，可以优化性能
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    @Transactional // 默认事务（读写）
    public Student createStudent(Student student) {
        // 可以在这里添加创建前的业务校验逻辑，例如检查studentId是否已存在
        Optional<Student> existingStudent = studentRepository.findByStudentId(student.getStudentId());
        if (existingStudent.isPresent()) {
            throw new IllegalArgumentException("学号 " + student.getStudentId() + " 已存在。");
        }
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为 " + id + " 的学生。"));

        // 检查更新的学号是否与系统中其他学生冲突 (如果允许修改学号)
        if (studentDetails.getStudentId() != null && !studentDetails.getStudentId().equals(student.getStudentId())) {
            Optional<Student> studentWithNewStudentId = studentRepository.findByStudentId(studentDetails.getStudentId());
            if (studentWithNewStudentId.isPresent() && !studentWithNewStudentId.get().getId().equals(id)) { // Ensure it's not the same student
                throw new IllegalArgumentException("学号 " + studentDetails.getStudentId() + " 已被其他学生使用。");
            }
            student.setStudentId(studentDetails.getStudentId());
        }

        // Only update fields if they are provided in studentDetails (partial update consideration)
        // For simplicity, current script assumes all relevant fields are in studentDetails for update.
        student.setName(studentDetails.getName());
        student.setGender(studentDetails.getGender());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setClassName(studentDetails.getClassName());

        student.setMajor(studentDetails.getMajor());
        student.setGpa(studentDetails.getGpa());
        student.setPhotoUrl(studentDetails.getPhotoUrl());


        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生，无法删除。");
        }
        studentRepository.deleteById(id);
    }
}
