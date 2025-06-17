package com.example.student_management_system.mapper;

import com.example.student_management_system.model.CourseOffering;
import com.example.student_management_system.model.OfferingMajorLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OfferingMajorLinkMapper {
    void insert(OfferingMajorLink link);
    void deleteByOfferingId(Long offeringId);
    List<CourseOffering> findOfferingsByMajorAndTimetable(
            @Param("majorId") Long majorId,
            @Param("academicYear") int academicYear,
            @Param("semester") int semester,
            @Param("courseDay") int courseDay,
            @Param("courseTime") int courseTime,
            @Param("excludeOfferingId") Long excludeOfferingId
    );
    List<CourseOffering> findCompulsoryOfferingsForMajor(
            @Param("majorId") Long majorId,
            @Param("academicYear") int academicYear,
            @Param("semester") int semester
    );
}