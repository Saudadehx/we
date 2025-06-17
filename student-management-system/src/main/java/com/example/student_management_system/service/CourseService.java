package com.example.student_management_system.service;

import com.example.student_management_system.dto.CourseCatalogDTO;
import com.example.student_management_system.mapper.*;
import com.example.student_management_system.model.*;
import com.example.student_management_system.event.CourseOfferingUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
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

    @Autowired
    public CourseService(CourseCatalogMapper courseCatalogMapper, CourseOfferingMapper courseOfferingMapper,
                         OfferingMajorLinkMapper offeringMajorLinkMapper, TeacherMapper teacherMapper, MajorMapper majorMapper,
                         ApplicationEventPublisher eventPublisher) { // ✨ 修改构造函数
        this.courseCatalogMapper = courseCatalogMapper;
        this.courseOfferingMapper = courseOfferingMapper;
        this.offeringMajorLinkMapper = offeringMajorLinkMapper;
        this.teacherMapper = teacherMapper;
        this.majorMapper = majorMapper;
        this.eventPublisher = eventPublisher; // ✨ 新增
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

        int usageCount = courseOfferingMapper.countByCourseCatalogId(id);
        if (usageCount > 0) {
            throw new IllegalArgumentException("无法删除该课程目录，因为它已被 " + usageCount + " 个课程安排所使用。");
        }

        courseCatalogMapper.deleteById(id);
    }


    // --- 课程安排管理 ---
    public List<CourseOffering> getAllOfferings() {
        return courseOfferingMapper.findAllWithDetails();
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

        eventPublisher.publishEvent(new CourseOfferingUpdatedEvent(this, offering));
        return offering;
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

        eventPublisher.publishEvent(new CourseOfferingUpdatedEvent(this, offering));
        return offering;
    }


    public void deleteOffering(Long offeringId) {
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

    @Transactional(readOnly = true)
    public List<CourseOffering> findOfferingsByTeacherId(Long teacherId) {
        return courseOfferingMapper.findOfferingsByTeacherId(teacherId);
    }
}