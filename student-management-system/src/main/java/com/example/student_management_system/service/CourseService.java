package com.example.student_management_system.service;

import com.example.student_management_system.mapper.*;
import com.example.student_management_system.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    private final CourseCatalogMapper courseCatalogMapper;
    private final CourseOfferingMapper courseOfferingMapper;
    private final OfferingMajorLinkMapper offeringMajorLinkMapper;
    private final TeacherMapper teacherMapper;
    private final MajorMapper majorMapper;

    @Autowired
    public CourseService(CourseCatalogMapper courseCatalogMapper, CourseOfferingMapper courseOfferingMapper, OfferingMajorLinkMapper offeringMajorLinkMapper, TeacherMapper teacherMapper, MajorMapper majorMapper) {
        this.courseCatalogMapper = courseCatalogMapper;
        this.courseOfferingMapper = courseOfferingMapper;
        this.offeringMajorLinkMapper = offeringMajorLinkMapper;
        this.teacherMapper = teacherMapper;
        this.majorMapper = majorMapper;
    }

    // --- 课程目录管理 ---
    public List<CourseCatalog> getAllCatalogs() {
        return courseCatalogMapper.findAll();
    }

    public CourseCatalog createCatalog(CourseCatalog catalog) {
        if (courseCatalogMapper.findByCourseCode(catalog.getCourseCode()) != null) {
            throw new IllegalArgumentException("课程代码 " + catalog.getCourseCode() + " 已存在。");
        }
        courseCatalogMapper.insert(catalog);
        return catalog;
    }

    // --- 课程安排管理 ---
    public List<CourseOffering> getAllOfferings() {
        return courseOfferingMapper.findAllWithDetails();
    }

    public CourseOffering createOffering(CourseOffering offering) {
        checkForConflicts(offering);
        courseOfferingMapper.insert(offering); // id会回填

        if (offering.getAssociatedMajors() != null && !offering.getAssociatedMajors().isEmpty()) {
            for (CourseOffering.MajorInfo majorInfo : offering.getAssociatedMajors()) {
                OfferingMajorLink link = new OfferingMajorLink();
                link.setCourseOfferingId(offering.getId());
                link.setMajorId(majorInfo.getMajorId());
                link.setCourseType(majorInfo.getCourseType());
                offeringMajorLinkMapper.insert(link);
            }
        }
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
        return offering;
    }

    public void deleteOffering(Long offeringId) {
        // Mybatis 的外键级联删除会处理 links 和 enrollments
        courseOfferingMapper.deleteById(offeringId);
    }

    private void checkForConflicts(CourseOffering offering) {
        if (offering.getCourseDay() == null || offering.getCourseTime() == null) {
            return; // 不检查没有安排时间的课程
        }

        // 教师冲突检测
        if (offering.getTeacherId() != null) {
            List<CourseOffering> teacherConflicts = courseOfferingMapper.findOfferingsByTeacherAndTimetable(
                    offering.getTeacherId(), offering.getAcademicYear(), offering.getSemester(),
                    offering.getCourseDay(), offering.getCourseTime(), offering.getId());
            if (!teacherConflicts.isEmpty()) {
                Teacher teacher = teacherMapper.findById(offering.getTeacherId());
                throw new IllegalArgumentException("教师冲突: " + teacher.getName() + " 在该时间已有其他安排。");
            }
        }

        // 专业班级冲突检测
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
    /**
     * 根据教师ID查找其所有课程安排
     * @param teacherId 教师的数据库ID
     * @return 课程安排列表
     */
    @Transactional(readOnly = true)
    public List<CourseOffering> findOfferingsByTeacherId(Long teacherId) {
        return courseOfferingMapper.findOfferingsByTeacherId(teacherId);
    }
}