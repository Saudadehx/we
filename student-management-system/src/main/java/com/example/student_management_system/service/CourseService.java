package com.example.student_management_system.service;

import com.example.student_management_system.dto.CourseCatalogDTO;
import com.example.student_management_system.event.CourseOfferingUpdatedEvent;
import com.example.student_management_system.mapper.*;
import com.example.student_management_system.model.*;
import com.example.student_management_system.model.Class;
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
    // 【修改】注入新的 OfferingClassLinkMapper
    private final OfferingClassLinkMapper offeringClassLinkMapper;
    private final TeacherMapper teacherMapper;
    // 【修改】注入新的 ClassMapper
    private final ClassMapper classMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EnrollmentMapper enrollmentMapper;
    private final ClassroomMapper classroomMapper;

    @Autowired
    public CourseService(CourseCatalogMapper courseCatalogMapper, CourseOfferingMapper courseOfferingMapper,
                         OfferingClassLinkMapper offeringClassLinkMapper, TeacherMapper teacherMapper, ClassMapper classMapper,
                         ApplicationEventPublisher eventPublisher, EnrollmentMapper enrollmentMapper,ClassroomMapper classroomMapper) {
        this.courseCatalogMapper = courseCatalogMapper;
        this.courseOfferingMapper = courseOfferingMapper;
        this.offeringClassLinkMapper = offeringClassLinkMapper;
        this.teacherMapper = teacherMapper;
        this.classMapper = classMapper;
        this.eventPublisher = eventPublisher;
        this.enrollmentMapper = enrollmentMapper;
        this.classroomMapper = classroomMapper;
    }

    // --- 课程目录管理 (无变化) ---
    // ... (createCatalog, updateCatalog, etc. remain the same)
    private CourseCatalogDTO convertToDto(CourseCatalog entity) {
        CourseCatalogDTO dto = new CourseCatalogDTO();
        dto.setId(entity.getId());
        dto.setCourseCode(entity.getCourseCode());
        dto.setName(entity.getName());
        dto.setCredits(entity.getCredits());
        dto.setLessonsPerWeek(entity.getLessonsPerWeek()); // 【代码新增】
        return dto;
    }

    private CourseCatalog convertToEntity(CourseCatalogDTO dto) {
        CourseCatalog entity = new CourseCatalog();
        entity.setId(dto.getId());
        entity.setCourseCode(dto.getCourseCode());
        entity.setName(dto.getName());
        entity.setCredits(dto.getCredits());
        entity.setLessonsPerWeek(dto.getLessonsPerWeek()); // 【代码新增】
        return entity;
    }
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

        catalogDto.setId(catalog.getId());
        return catalogDto;
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
        existing.setLessonsPerWeek(catalogDto.getLessonsPerWeek()); // 【代码新增】

        courseCatalogMapper.update(existing);
        return convertToDto(existing);
    }

    public void deleteCatalog(Long id) {
        if (courseCatalogMapper.findById(id) == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的课程目录。");
        }
        List<Long> offeringIdsToDelete = courseOfferingMapper.findOfferingIdsByCatalogId(id);
        for (Long offeringId : offeringIdsToDelete) {
            log.info("级联删除：正在清理与课程安排ID {} 相关的记录...", offeringId);
            enrollmentMapper.deleteByCourseOfferingId(offeringId);
            // 【修改】从 major link 改为 class link
            offeringClassLinkMapper.deleteByOfferingId(offeringId);
            courseOfferingMapper.deleteById(offeringId);
            log.info("级联删除：已清理课程安排ID {}", offeringId);
        }
        courseCatalogMapper.deleteById(id);
    }

    // --- 课程安排管理 ---

    public List<CourseOffering> getAllOfferings() {
        return courseOfferingMapper.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<CourseOffering> findOfferingsByTeacherId(Long teacherId) {
        return courseOfferingMapper.findOfferingsByTeacherId(teacherId);
    }


    public CourseOffering createOffering(CourseOffering offering) {
        checkForConflicts(offering);
        courseOfferingMapper.insert(offering);

        // 【修改】处理 associatedClasses
        if (offering.getAssociatedClasses() != null && !offering.getAssociatedClasses().isEmpty()) {
            for (CourseOffering.ClassInfo classInfo : offering.getAssociatedClasses()) {
                OfferingClassLink link = new OfferingClassLink();
                link.setCourseOfferingId(offering.getId());
                link.setClassId(classInfo.getClassId());
                link.setCourseType(classInfo.getCourseType());
                offeringClassLinkMapper.insert(link);
            }
        }

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

        // 【修改】先删后增，更新 associatedClasses
        offeringClassLinkMapper.deleteByOfferingId(offering.getId());
        if (offering.getAssociatedClasses() != null && !offering.getAssociatedClasses().isEmpty()) {
            for (CourseOffering.ClassInfo classInfo : offering.getAssociatedClasses()) {
                OfferingClassLink link = new OfferingClassLink();
                link.setCourseOfferingId(offering.getId());
                link.setClassId(classInfo.getClassId());
                link.setCourseType(classInfo.getCourseType());
                offeringClassLinkMapper.insert(link);
            }
        }

        CourseOffering updatedOfferingWithDetails = courseOfferingMapper.findById(offering.getId());
        eventPublisher.publishEvent(new CourseOfferingUpdatedEvent(this, updatedOfferingWithDetails));
        return updatedOfferingWithDetails;
    }


    public void deleteOffering(Long offeringId) {
        enrollmentMapper.deleteByCourseOfferingId(offeringId);
        // 【修改】删除 class link
        offeringClassLinkMapper.deleteByOfferingId(offeringId);
        courseOfferingMapper.deleteById(offeringId);
    }

    // 【修改】冲突检查逻辑
    private void checkForConflicts(CourseOffering offering) {
        if (offering.getCourseDay() == null || offering.getCourseTime() == null) {
            return;
        }

        // 教师时间冲突检查
        if (offering.getTeacherId() != null) {
            List<CourseOffering> teacherConflicts = courseOfferingMapper.findOfferingsByTeacherAndTimetable(
                    offering.getTeacherId(), offering.getAcademicYear(), offering.getSemester(),
                    offering.getCourseDay(), offering.getCourseTime(), offering.getId());
            if (!teacherConflicts.isEmpty()) {
                Teacher teacher = teacherMapper.findById(offering.getTeacherId());
                throw new IllegalArgumentException("教师冲突: " + teacher.getName() + " 在该时间已有其他安排。");
            }
        }
        // 教室时间冲突检查
        if (offering.getClassroomId() != null) {
            List<CourseOffering> classroomConflicts = courseOfferingMapper.findOfferingsByClassroomAndTimetable(
                    offering.getClassroomId(), offering.getAcademicYear(), offering.getSemester(),
                    offering.getCourseDay(), offering.getCourseTime(), offering.getId());
            if (!classroomConflicts.isEmpty()) {
                Classroom classroom = classroomMapper.findById(offering.getClassroomId());
                throw new IllegalArgumentException("教室冲突: " + classroom.getName() + " 在该时间已被其他课程占用。");
            }
        }

        // 【新增】检查课程容量是否小于教室容量
        if (offering.getCapacity() != null && offering.getClassroomId() != null) {
            Classroom classroom = classroomMapper.findById(offering.getClassroomId());
            if (offering.getCapacity() > classroom.getCapacity()) {
                throw new IllegalArgumentException("课程容量 (" + offering.getCapacity() + ") 不能大于教室容量 (" + classroom.getCapacity() + ")。");
            }
        }

        // 【修改】班级时间冲突检查
        if (offering.getAssociatedClasses() != null) {
            for (CourseOffering.ClassInfo classInfo : offering.getAssociatedClasses()) {
                List<CourseOffering> classConflicts = offeringClassLinkMapper.findOfferingsByClassAndTimetable(
                        classInfo.getClassId(), offering.getAcademicYear(), offering.getSemester(),
                        offering.getCourseDay(), offering.getCourseTime(), offering.getId());
                if (!classConflicts.isEmpty()) {
                    Class cls = classMapper.findById(classInfo.getClassId());
                    throw new IllegalArgumentException("班级冲突: " + cls.getName() + " 在该时间已有其他课程。");
                }
            }
        }
    }
}