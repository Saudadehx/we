package com.example.student_management_system.service;

import com.example.student_management_system.dto.CourseCatalogDTO;
import com.example.student_management_system.event.CourseOfferingUpdatedEvent;
import com.example.student_management_system.mapper.*;
import com.example.student_management_system.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CourseService {

    private final CourseCatalogMapper courseCatalogMapper;
    private final CourseOfferingMapper courseOfferingMapper;
    private final OfferingMajorLinkMapper offeringMajorLinkMapper;
    private final TeacherMapper teacherMapper;
    private final MajorMapper majorMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EnrollmentMapper enrollmentMapper;

    @Autowired
    public CourseService(CourseCatalogMapper courseCatalogMapper, CourseOfferingMapper courseOfferingMapper,
                         OfferingMajorLinkMapper offeringMajorLinkMapper, TeacherMapper teacherMapper, MajorMapper majorMapper,
                         ApplicationEventPublisher eventPublisher, EnrollmentMapper enrollmentMapper) {
        this.courseCatalogMapper = courseCatalogMapper;
        this.courseOfferingMapper = courseOfferingMapper;
        this.offeringMajorLinkMapper = offeringMajorLinkMapper;
        this.teacherMapper = teacherMapper;
        this.majorMapper = majorMapper;
        this.eventPublisher = eventPublisher;
        this.enrollmentMapper = enrollmentMapper;
    }

    private CourseCatalogDTO convertToDto(CourseCatalog entity) {
        CourseCatalogDTO dto = new CourseCatalogDTO();
        dto.setId(entity.getId());
        dto.setCourseCode(entity.getCourseCode());
        dto.setName(entity.getName());
        dto.setCredits(entity.getCredits());
        return dto;
    }

    private CourseCatalog convertToEntity(CourseCatalogDTO dto) {
        CourseCatalog entity = new CourseCatalog();
        entity.setId(dto.getId());
        entity.setCourseCode(dto.getCourseCode());
        entity.setName(dto.getName());
        entity.setCredits(dto.getCredits());
        return entity;
    }


    // --- 课程目录管理 ---
    public List<CourseCatalogDTO> getAllCatalogs() {
        return courseCatalogMapper.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CourseCatalogDTO createCatalog(CourseCatalogDTO catalogDto) {
        if (courseCatalogMapper.findByCourseCode(catalogDto.getCourseCode()) != null) {
            throw new IllegalArgumentException("课程代码 " + catalogDto.getCourseCode() + " 已存在。");
        }
        CourseCatalog catalog = convertToEntity(catalogDto);
        courseCatalogMapper.insert(catalog);
        return convertToDto(catalog);
    }

    public CourseCatalogDTO updateCatalog(Long id, CourseCatalogDTO catalogDto) {
        CourseCatalog existing = courseCatalogMapper.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的课程目录。");
        }

        CourseCatalog conflict = courseCatalogMapper.findByCourseCode(catalogDto.getCourseCode());
        if (conflict != null && !conflict.getId().equals(id)) {
            throw new IllegalArgumentException("课程代码 " + catalogDto.getCourseCode() + " 已被其他课程使用。");
        }

        existing.setCourseCode(catalogDto.getCourseCode());
        existing.setName(catalogDto.getName());
        existing.setCredits(catalogDto.getCredits());

        courseCatalogMapper.update(existing);
        return convertToDto(existing);
    }

    public void deleteCatalog(Long id) {
        if (courseCatalogMapper.findById(id) == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的课程目录。");
        }

        // 【核心修复】将原有的检查逻辑替换为级联删除逻辑
        // 1. 查找所有使用此课程目录的课程安排ID
        List<Long> offeringIdsToDelete = courseOfferingMapper.findOfferingIdsByCatalogId(id);

        // 2. 遍历这些ID，并对每一个课程安排执行完整的删除操作
        for (Long offeringId : offeringIdsToDelete) {
            log.info("级联删除：正在清理与课程安排ID {} 相关的记录...", offeringId);
            // 2a. 删除与该安排关联的学生选课记录
            enrollmentMapper.deleteByCourseOfferingId(offeringId);
            // 2b. 删除与该安排关联的专业链接
            offeringMajorLinkMapper.deleteByOfferingId(offeringId);
            // 2c. 删除该安排本身
            courseOfferingMapper.deleteById(offeringId);
            log.info("级联删除：已清理课程安排ID {}", offeringId);
        }

        // 3. 在所有依赖项都被清理干净后，最后删除课程目录本身
        courseCatalogMapper.deleteById(id);
    }


    // --- 课程安排管理 ---

    // 【优化】: 直接返回MyBatis处理好的结果，不再需要在Java中手动去重合并
    public List<CourseOffering> getAllOfferings() {
        return courseOfferingMapper.findAllWithDetails();
    }

    // 【优化】: 直接返回MyBatis处理好的结果
    @Transactional(readOnly = true)
    public List<CourseOffering> findOfferingsByTeacherId(Long teacherId) {
        return courseOfferingMapper.findOfferingsByTeacherId(teacherId);
    }


    public CourseOffering createOffering(CourseOffering offering) {
        checkForConflicts(offering);
        courseOfferingMapper.insert(offering);

        if (offering.getAssociatedMajors() != null && !offering.getAssociatedMajors().isEmpty()) {
            for (CourseOffering.MajorInfo majorInfo : offering.getAssociatedMajors()) {
                OfferingMajorLink link = new OfferingMajorLink();
                link.setCourseOfferingId(offering.getId());
                link.setMajorId(majorInfo.getMajorId());
                link.setCourseType(majorInfo.getCourseType());
                offeringMajorLinkMapper.insert(link);
            }
        }

        // 【优化】: 创建后，重新从数据库获取完整的、聚合好的课程信息用于发布事件
        CourseOffering createdOfferingWithDetails = courseOfferingMapper.findById(offering.getId());
        eventPublisher.publishEvent(new CourseOfferingUpdatedEvent(this, createdOfferingWithDetails));
        return createdOfferingWithDetails;
    }

    public CourseOffering updateOffering(CourseOffering offering) {
        CourseOffering existing = courseOfferingMapper.findById(offering.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("未找到ID为 " + offering.getId() + " 的课程安排");
        }

        checkForConflicts(offering);
        courseOfferingMapper.update(offering);

        offeringMajorLinkMapper.deleteByOfferingId(offering.getId());
        if (offering.getAssociatedMajors() != null && !offering.getAssociatedMajors().isEmpty()) {
            for (CourseOffering.MajorInfo majorInfo : offering.getAssociatedMajors()) {
                OfferingMajorLink link = new OfferingMajorLink();
                link.setCourseOfferingId(offering.getId());
                link.setMajorId(majorInfo.getMajorId());
                link.setCourseType(majorInfo.getCourseType());
                offeringMajorLinkMapper.insert(link);
            }
        }

        // 【优化】: 更新后，同样重新获取完整信息再发布事件
        CourseOffering updatedOfferingWithDetails = courseOfferingMapper.findById(offering.getId());
        eventPublisher.publishEvent(new CourseOfferingUpdatedEvent(this, updatedOfferingWithDetails));
        return updatedOfferingWithDetails;
    }


    public void deleteOffering(Long offeringId) {
        // ✨ 关键修复：在删除课程安排之前，必须先删除所有关联的选课记录，以避免数据库外键约束冲突。
        enrollmentMapper.deleteByCourseOfferingId(offeringId);
        offeringMajorLinkMapper.deleteByOfferingId(offeringId);
        courseOfferingMapper.deleteById(offeringId);
    }

    private void checkForConflicts(CourseOffering offering) {
        if (offering.getCourseDay() == null || offering.getCourseTime() == null) {
            return;
        }

        if (offering.getTeacherId() != null) {
            List<CourseOffering> teacherConflicts = courseOfferingMapper.findOfferingsByTeacherAndTimetable(
                    offering.getTeacherId(), offering.getAcademicYear(), offering.getSemester(),
                    offering.getCourseDay(), offering.getCourseTime(), offering.getId());
            if (!teacherConflicts.isEmpty()) {
                Teacher teacher = teacherMapper.findById(offering.getTeacherId());
                throw new IllegalArgumentException("教师冲突: " + teacher.getName() + " 在该时间已有其他安排。");
            }
        }

        if (offering.getAssociatedMajors() != null) {
            for (CourseOffering.MajorInfo majorInfo : offering.getAssociatedMajors()) {
                List<CourseOffering> majorConflicts = offeringMajorLinkMapper.findOfferingsByMajorAndTimetable(
                        majorInfo.getMajorId(), offering.getAcademicYear(), offering.getSemester(),
                        offering.getCourseDay(), offering.getCourseTime(), offering.getId());
                if (!majorConflicts.isEmpty()) {
                    Major major = majorMapper.findById(majorInfo.getMajorId());
                    throw new IllegalArgumentException("专业冲突: " + major.getName() + " 在该时间已有其他课程。");
                }
            }
        }
    }
}