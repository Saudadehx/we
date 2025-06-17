package com.example.student_management_system.mapper;

import com.example.student_management_system.model.Teacher;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface TeacherMapper {

    /**
     * 根据教师工号查找教师，主要用于登录验证。
     * @param teacherId 教师工号
     * @return 找到的教师对象，否则返回null
     */
    Teacher findByTeacherId(String teacherId);

    /**
     * 查找所有教师，用于管理员的教师管理界面。
     * @return 教师列表
     */
    List<Teacher> findAll();

    /**
     * 根据数据库主键ID查找教师。
     * @param id 教师的数据库ID
     * @return 找到的教师对象，否则返回null
     */
    Teacher findById(Long id);

    /**
     * 根据多个数据库主键ID查找教师。
     * @param ids 教师的数据库ID列表
     * @return 找到的教师对象列表
     */
    List<Teacher> findByIds(List<Long> ids); // 新增方法

    /**
     * 插入一个新的教师记录。
     * @param teacher 新的教师对象
     */
    void insert(Teacher teacher);

    // 【新增】更新教师信息
    int update(Teacher teacher);

    // 【新增】根据ID删除教师
    void deleteById(Long id);


    // 您可以根据需要添加 update 和 deleteById 方法
}