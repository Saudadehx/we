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
    int countByCourseOfferingId(Long courseOfferingId);

    /**
     * 【新增方法】根据课程安排ID删除所有相关的选课记录。
     * 这是解决删除冲突的关键。
     * @param courseOfferingId 课程安排的ID
     */
    void deleteByCourseOfferingId(Long courseOfferingId);
}