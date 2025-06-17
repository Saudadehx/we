package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EnrollmentMapper {
    List<Enrollment> findByStudentId(Long studentId);

    // 将 findByCourseId 修改为 findByCourseOfferingId
    List<Enrollment> findByCourseOfferingId(Long courseOfferingId);

    void insert(Enrollment enrollment);

    int updateScore(@Param("id") Long id, @Param("score") Double score);

    void deleteById(Long id);

    Enrollment findById(Long id);
}