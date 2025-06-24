package com.example.student_management_system.service;

import com.example.student_management_system.dto.DashboardStatsDTO;
import com.example.student_management_system.dto.StudentDTO;
import com.example.student_management_system.mapper.MajorMapper;
import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.model.Major;
import com.example.student_management_system.model.Student;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService { // 学生服务类

    private final StudentMapper studentMapper; // 学生数据访问层
    private final PasswordEncoder passwordEncoder; // 密码加密器
    private final MajorMapper majorMapper;// 专业数据访问层
    private final EnrollmentService enrollmentService;// 选课服务

    @Autowired
    public StudentService(StudentMapper studentMapper, PasswordEncoder passwordEncoder, MajorMapper majorMapper, EnrollmentService enrollmentService) {
        this.studentMapper = studentMapper;// 学生数据访问层
        this.passwordEncoder = passwordEncoder;// 密码加密器
        this.majorMapper = majorMapper;// 专业数据访问层
        this.enrollmentService = enrollmentService;// 选课服务
    }

    // --- 数据转换辅助方法 ---

    private StudentDTO convertToDto(Student student) {// 将 Student 实体转换为 StudentDTO
        if (student == null) return null;// 如果学生对象为空，直接返回 null

        StudentDTO dto = new StudentDTO();// 创建一个新的 StudentDTO 对象
        BeanUtils.copyProperties(student, dto); // student 中已经有了 className 和 majorName

        // 如果连接查询没有查出名称，则提供默认值
        if (student.getMajorId() != null && student.getMajorName() == null) {
            Major major = majorMapper.findById(student.getMajorId());
            dto.setMajorName(major != null ? major.getName() : "未知专业");
        } else if (student.getMajorName() == null) {
            dto.setMajorName("未分配");
        }
        // 如果 className 为空，可能是因为没有连接查询到班级名称
        if (student.getClassId() != null && student.getClassName() == null) {
            // 这里可以添加一个 classMapper.findById() 来作为备用方案
            dto.setClassName("未知班级");
        } else if (student.getClassName() == null) {
            dto.setClassName("未分配");
        }
        // 设置学术信息
        return dto;
    }
    // 将 StudentDTO 转换为 Student 实体
    private void updateEntityFromDto(Student student, StudentDTO dto) {
        // 只复制允许修改的字段，避免覆盖敏感信息
        BeanUtils.copyProperties(dto, student, "id", "password", "majorName", "className");
    }


    // --- 公共服务方法 ---

    public DashboardStatsDTO getDashboardStats() {
        List<Student> students = studentMapper.findAll(Collections.emptyMap());
        List<Major> majors = majorMapper.findAll();

        long totalStudents = students.size();
        long totalClasses = students.stream().map(Student::getClassName).distinct().count();
        long totalMajors = majors.size();
        long totalGrades = students.stream()
                .map(s -> s.getClassName().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(0, Math.min(s.length(), 4)))
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
    public List<StudentDTO> getAllStudents() {
        return studentMapper.findAll(Collections.emptyMap()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<StudentDTO> getStudentById(Long id) {
        Student student = studentMapper.findById(id);
        return Optional.ofNullable(convertToDto(student));
    }

    @Transactional
    public StudentDTO createStudent(StudentDTO studentDto) {
        if (studentMapper.findByStudentId(studentDto.getStudentId()) != null) {
            throw new IllegalArgumentException("学号 " + studentDto.getStudentId() + " 已存在。");
        }
        if (!StringUtils.hasText(studentDto.getPassword())) {
            throw new IllegalArgumentException("创建学生时，初始密码不能为空。");
        }

        Student student = new Student();
        updateEntityFromDto(student, studentDto);
        student.setPassword(passwordEncoder.encode(studentDto.getPassword()));

        studentMapper.insert(student);
        Student createdStudent = studentMapper.findByStudentId(student.getStudentId());

        enrollmentService.assignCompulsoryCoursesForStudent(createdStudent);

        return convertToDto(createdStudent);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDetails) {
        // 首先检查学生是否存在
        Student student = studentMapper.findById(id); // 根据ID查询学生
        if (student == null) { // 如果未找到学生，抛出异常
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生。");
        }
        // 记录旧的学术信息
        Long oldMajorId = student.getMajorId(); // 记录旧的专业ID
        Integer oldAcademicYear = student.getAcademicYear(); // 记录旧的学年
        Integer oldSemester = student.getSemester();  // 记录旧的学期
        // 检查学号是否被修改
        if (studentDetails.getStudentId() != null && !studentDetails.getStudentId().equals(student.getStudentId())) {// 如果学号被修改
            if (studentMapper.findByStudentId(studentDetails.getStudentId()) != null) { // 检查新学号是否已存在
                throw new IllegalArgumentException("学号 " + studentDetails.getStudentId() + " 已被其他学生使用。");
            }
        }
        // 更新学生实体的属性
        updateEntityFromDto(student, studentDetails); // 更新学生实体的属性
        // 如果提供了新密码，则进行加密
        if (StringUtils.hasText(studentDetails.getPassword())) { // 如果提供了新密码，则进行加密
            student.setPassword(passwordEncoder.encode(studentDetails.getPassword())); // 加密新密码
        }
        // 更新学生信息
        int affectedRows = studentMapper.update(student); // 更新学生信息
        if (affectedRows == 0) { // 如果没有行被更新，说明数据可能已被其他人修改
            throw new RuntimeException("更新学生信息失败，数据可能已被他人修改，请刷新后重试。");
        }
        // 在更新后检查学术信息是否有变更
        boolean academicInfoChanged = !Objects.equals(oldMajorId, student.getMajorId()) ||
                !Objects.equals(oldAcademicYear, student.getAcademicYear()) ||
                !Objects.equals(oldSemester, student.getSemester());
        // 如果学术信息有变更，则重新分配必修课
        if (academicInfoChanged) {
            enrollmentService.reconcileEnrollmentsForStudent(student);
        }
        // 返回更新后的学生DTO
        return convertToDto(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (studentMapper.findById(id) == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生，无法删除。");
        }
        studentMapper.deleteById(id);
    }

    @Transactional
    public StudentDTO updateStudentProfile(Long studentId, StudentDTO profileDetails) {
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + studentId + " 的学生。");
        }

        // ✨【修正】恢复到更安全的“白名单”模式，只更新允许学生修改的字段。
        if (StringUtils.hasText(profileDetails.getPassword())) {
            student.setPassword(passwordEncoder.encode(profileDetails.getPassword()));
        }
        student.setPhoneNumber(profileDetails.getPhoneNumber());
        student.setEmail(profileDetails.getEmail());
        student.setEthnicity(profileDetails.getEthnicity());
        student.setNativePlace(profileDetails.getNativePlace());
        student.setPoliticalStatus(profileDetails.getPoliticalStatus());

        int affectedRows = studentMapper.update(student);
        if (affectedRows == 0) {
            throw new RuntimeException("更新个人信息失败，请刷新后重试。");
        }

        return convertToDto(student);
    }
}