package com.example.student_management_system.service;

import com.example.student_management_system.dto.MajorDTO;
import com.example.student_management_system.mapper.MajorMapper;
import com.example.student_management_system.model.Major;
import org.springframework.beans.BeanUtils; // ✨ 导入 Spring 的 BeanUtils
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MajorService {

    @Autowired
    private MajorMapper majorMapper;

    // ✨ 【优化】使用 BeanUtils.copyProperties 简化 DTO 转换
    private MajorDTO convertToDto(Major major) {
        MajorDTO dto = new MajorDTO();
        BeanUtils.copyProperties(major, dto);
        return dto;
    }

    // ✨ 【优化】使用 BeanUtils.copyProperties 简化实体转换
    private Major convertToEntity(MajorDTO dto) {
        Major major = new Major();
        BeanUtils.copyProperties(dto, major);
        return major;
    }

    @Transactional(readOnly = true)
    public List<MajorDTO> getAllMajors() {
        return majorMapper.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public MajorDTO createMajor(MajorDTO majorDTO) {
        if (majorMapper.findByName(majorDTO.getName()) != null) {
            throw new IllegalArgumentException("专业名称 '" + majorDTO.getName() + "' 已存在。");
        }
        Major major = convertToEntity(majorDTO);
        majorMapper.insert(major);
        // ✨ 创建后，将数据库生成的ID设置回DTO
        majorDTO.setId(major.getId());
        return majorDTO;
    }

    @Transactional
    public MajorDTO updateMajor(Long id, MajorDTO majorDTO) {
        Major existingMajor = majorMapper.findById(id);
        if (existingMajor == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的专业不存在。");
        }
        Major majorWithSameName = majorMapper.findByName(majorDTO.getName());
        if (majorWithSameName != null && !majorWithSameName.getId().equals(id)) {
            throw new IllegalArgumentException("专业名称 '" + majorDTO.getName() + "' 已被其他专业使用。");
        }

        // ✨ 使用 BeanUtils 简化属性复制
        BeanUtils.copyProperties(majorDTO, existingMajor, "id"); // 忽略 "id" 属性，防止它被覆盖
        majorMapper.update(existingMajor);
        return convertToDto(existingMajor);
    }

    @Transactional
    public void deleteMajor(Long id) {
        if (majorMapper.findById(id) == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的专业不存在，无法删除。");
        }
        // 在实际项目中，删除前还应检查是否有学生或课程关联了此专业
        majorMapper.deleteById(id);
    }
}