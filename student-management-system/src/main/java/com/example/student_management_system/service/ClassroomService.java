package com.example.student_management_system.service;

import com.example.student_management_system.mapper.ClassroomMapper;
import com.example.student_management_system.model.Classroom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClassroomService {

    @Autowired
    private ClassroomMapper classroomMapper;

    public List<Classroom> getAllClassrooms() {
        return classroomMapper.findAll();
    }

    public Classroom createClassroom(Classroom classroom) {
        classroomMapper.insert(classroom);
        return classroom;
    }

    public Classroom updateClassroom(Long id, Classroom classroomDetails) {
        Classroom classroom = classroomMapper.findById(id);
        if (classroom == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的教室。");
        }
        classroomDetails.setId(id);
        classroomMapper.update(classroomDetails);
        return classroomDetails;
    }

    public void deleteClassroom(Long id) {
        // 在实际应用中，删除前应检查该教室是否被任何课程安排使用
        classroomMapper.deleteById(id);
    }
}