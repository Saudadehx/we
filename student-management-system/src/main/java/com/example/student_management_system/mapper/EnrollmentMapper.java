package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EnrollmentMapper {
    // 为了方便，我们这里可以定义一个更复杂的返回类型，或者在Service层组装
    // 此处为了简化，我们只返回基础的Enrollment对象

    List<Enrollment> findByStudentId(Long studentId); // 学生查自己的所有成绩
    List<Enrollment> findByCourseId(Long courseId);   // 教师查某门课的所有学生选课记录

    void insert(Enrollment enrollment); // 管理员为学生选课

    /**
     * 更新成绩
     * @param id enrollment记录的ID
     * @param score 新的分数
     * @return 更新的行数
     */
    int updateScore(@Param("id") Long id, @Param("score") Double score);

    void deleteById(Long id); // 管理员取消学生选课

    Enrollment findById(Long enrollmentId);
}