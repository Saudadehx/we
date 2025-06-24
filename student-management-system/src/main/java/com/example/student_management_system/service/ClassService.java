package com.example.student_management_system.service;

import com.example.student_management_system.dto.ClassDTO;
import com.example.student_management_system.mapper.ClassMapper;
import com.example.student_management_system.mapper.MajorMapper;
import com.example.student_management_system.model.Class;
import com.example.student_management_system.model.Major;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassService {

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private MajorMapper majorMapper;

    private ClassDTO convertToDto(Class entity) {
        ClassDTO dto = new ClassDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ClassDTO> getAllClasses() {
        return classMapper.findAllWithMajor().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassDTO createClass(ClassDTO classDTO) {
        Major major = majorMapper.findById(classDTO.getMajorId());
        if (major == null) {
            throw new ResourceNotFoundException("关联的专业ID " + classDTO.getMajorId() + " 不存在。");
        }

        Class newClass = new Class();
        newClass.setName(classDTO.getName());
        newClass.setMajorId(classDTO.getMajorId());

        classMapper.insert(newClass);
        classDTO.setId(newClass.getId());
        classDTO.setMajorName(major.getName());
        return classDTO;
    }

    @Transactional
    public ClassDTO updateClass(Long id, ClassDTO classDTO) {
        Class existingClass = classMapper.findById(id);
        if (existingClass == null) {
            throw new ResourceNotFoundException("班级ID " + id + " 不存在。");
        }
        Major major = majorMapper.findById(classDTO.getMajorId());
        if (major == null) {
            throw new ResourceNotFoundException("关联的专业ID " + classDTO.getMajorId() + " 不存在。");
        }

        existingClass.setName(classDTO.getName());
        existingClass.setMajorId(classDTO.getMajorId());
        classMapper.update(existingClass);

        classDTO.setId(id);
        classDTO.setMajorName(major.getName());
        return classDTO;
    }

    @Transactional
    public void deleteClass(Long id) {
        if (classMapper.findById(id) == null) {
            throw new ResourceNotFoundException("班级ID " + id + " 不存在，无法删除。");
        }
        // 注意：实际项目中，删除班级前应检查是否有学生关联
        classMapper.deleteById(id);
    }
}