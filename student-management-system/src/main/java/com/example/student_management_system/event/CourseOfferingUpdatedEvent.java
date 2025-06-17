package com.example.student_management_system.event;

import com.example.student_management_system.model.CourseOffering;
import org.springframework.context.ApplicationEvent;

/**
 * 当一个课程安排被创建或更新时发布的事件
 */
public class CourseOfferingUpdatedEvent extends ApplicationEvent {

    private final CourseOffering courseOffering;

    public CourseOfferingUpdatedEvent(Object source, CourseOffering courseOffering) {
        super(source);
        this.courseOffering = courseOffering;
    }

    public CourseOffering getCourseOffering() {
        return courseOffering;
    }
}