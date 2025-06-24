package com.example.student_management_system.service;

import com.example.student_management_system.mapper.ClassroomMapper;
import com.example.student_management_system.mapper.CourseCatalogMapper;
import com.example.student_management_system.mapper.CourseOfferingMapper;
import com.example.student_management_system.mapper.OfferingClassLinkMapper; // 【代码新增】
import com.example.student_management_system.model.Classroom;
import com.example.student_management_system.model.CourseCatalog;
import com.example.student_management_system.model.CourseOffering;
import com.example.student_management_system.model.OfferingClassLink; // 【代码新增】
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SchedulingService {

    private static final int POPULATION_SIZE = 200;
    private static final double MUTATION_PROBABILITY = 0.05;
    private static final double CROSSOVER_PROBABILITY = 0.3;
    private static final int MAX_GENERATIONS = 300;
    private static final int MAX_NO_IMPROVEMENT_GENERATIONS = 50;
    private static final int HARD_CONSTRAINT_PENALTY = 1000;
    private static final int SOFT_CONSTRAINT_PENALTY = 1;

    private final CourseOfferingMapper courseOfferingMapper;
    private final ClassroomMapper classroomMapper;
    private final CourseCatalogMapper courseCatalogMapper;
    private final SystemSettingService systemSettingService;
    private final OfferingClassLinkMapper offeringClassLinkMapper; // 【代码新增】

    @Autowired
    public SchedulingService(CourseOfferingMapper courseOfferingMapper, ClassroomMapper classroomMapper, CourseCatalogMapper courseCatalogMapper, SystemSettingService systemSettingService, OfferingClassLinkMapper offeringClassLinkMapper) { //【代码修改】
        this.courseOfferingMapper = courseOfferingMapper;
        this.classroomMapper = classroomMapper;
        this.courseCatalogMapper = courseCatalogMapper;
        this.systemSettingService = systemSettingService;
        this.offeringClassLinkMapper = offeringClassLinkMapper; // 【代码新增】
    }

    public record ScheduledUnit(CourseOffering offering, Integer dayOfWeek, Integer timeSlot, Classroom classroom) {}

    private int calculateFitness(final Genotype<IntegerGene> genotype, final List<List<ScheduledUnit>> scheduleSpace) {
        List<ScheduledUnit> currentSchedule = new ArrayList<>();
        for (int i = 0; i < genotype.chromosome().length(); i++) {
            int geneValue = genotype.chromosome().get(i).allele();
            currentSchedule.add(scheduleSpace.get(i).get(geneValue));
        }
        int penalty = 0;
        Map<String, Long> teacherSchedule = currentSchedule.stream()
                .filter(unit -> unit.offering().getTeacherId() != null && unit.dayOfWeek() != null && unit.timeSlot() != null)
                .collect(Collectors.groupingBy(
                        unit -> unit.offering().getTeacherId() + "-" + unit.dayOfWeek() + "-" + unit.timeSlot(),
                        Collectors.counting()
                ));
        penalty += teacherSchedule.values().stream().mapToInt(count -> (int) (count > 1 ? (count - 1) * HARD_CONSTRAINT_PENALTY : 0)).sum();
        Map<String, Long> classroomSchedule = currentSchedule.stream()
                .filter(unit -> unit.classroom() != null && unit.dayOfWeek() != null && unit.timeSlot() != null)
                .collect(Collectors.groupingBy(
                        unit -> unit.classroom().getId() + "-" + unit.dayOfWeek() + "-" + unit.timeSlot(),
                        Collectors.counting()
                ));
        penalty += classroomSchedule.values().stream().mapToInt(count -> (int) (count > 1 ? (count - 1) * HARD_CONSTRAINT_PENALTY : 0)).sum();
        Map<Long, List<ScheduledUnit>> classSchedules = currentSchedule.stream()
                .filter(unit -> unit.offering().getAssociatedClasses() != null && !unit.offering().getAssociatedClasses().isEmpty()) //【代码修改】增加非空判断
                .flatMap(unit -> unit.offering().getAssociatedClasses().stream()
                        .map(classInfo -> Map.entry(classInfo.getClassId(), unit)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        for (List<ScheduledUnit> unitsForClass : classSchedules.values()) {
            Map<String, Long> timeSlotCounts = unitsForClass.stream()
                    .filter(unit -> unit.dayOfWeek() != null && unit.timeSlot() != null)
                    .collect(Collectors.groupingBy(
                            unit -> unit.dayOfWeek() + "-" + unit.timeSlot(),
                            Collectors.counting()
                    ));
            penalty += timeSlotCounts.values().stream().mapToInt(count -> (int) (count > 1 ? (count - 1) * HARD_CONSTRAINT_PENALTY : 0)).sum();
        }
        for (ScheduledUnit unit : currentSchedule) {
            if (unit.offering().getCapacity() != null && unit.classroom() != null) {
                if (unit.offering().getCapacity() > unit.classroom().getCapacity()) {
                    penalty += (unit.offering().getCapacity() - unit.classroom().getCapacity()) * SOFT_CONSTRAINT_PENALTY;
                }
            }
        }
        return -penalty;
    }


    @Async
    @Transactional
    public void generateSchedule() {
        log.info("【排课任务开始】");
        long startTime = System.currentTimeMillis();
        log.info("正在加载排课所需的基础数据...");
        List<CourseCatalog> allCatalogs = courseCatalogMapper.findAll();
        List<Classroom> availableClassrooms = classroomMapper.findAll();
        int currentAcademicYear = systemSettingService.getCurrentAcademicYear();
        int currentSemester = systemSettingService.getCurrentSemester();
        log.info("当前排课周期: {}-{}学年, 第{}学期", currentAcademicYear, currentAcademicYear + 1, currentSemester);
        if (allCatalogs.isEmpty()) {
            log.warn("课程目录为空，任务终止。");
            return;
        }

        List<CourseOffering> offeringsToSchedule = new ArrayList<>();
        for (CourseCatalog catalog : allCatalogs) {
            List<CourseOffering> existingOfferings = courseOfferingMapper.findByCourseCatalogId(catalog.getId());

            List<CourseOffering> relevantOfferings = existingOfferings.stream()
                    .filter(o -> Objects.nonNull(o.getAcademicYear()) && Objects.nonNull(o.getSemester()) &&
                            o.getAcademicYear() == currentAcademicYear && o.getSemester() == currentSemester)
                    .map(o -> courseOfferingMapper.findById(o.getId())) // 【重要】重新获取完整信息，包含associatedClasses
                    .collect(Collectors.toList());

            int requiredCount = catalog.getLessonsPerWeek() != null ? catalog.getLessonsPerWeek() : 1;
            int currentCount = relevantOfferings.size();
            offeringsToSchedule.addAll(relevantOfferings);

            // 【核心逻辑：克隆模板以补齐课时】
            if (currentCount > 0 && currentCount < requiredCount) {
                // 使用第一个已存在的、信息完整的课程安排作为模板
                CourseOffering templateOffering = relevantOfferings.get(0);

                for (int i = 0; i < requiredCount - currentCount; i++) {
                    // 1. 克隆课程安排基本信息
                    CourseOffering newOffering = new CourseOffering();
                    newOffering.setCourseCatalogId(templateOffering.getCourseCatalogId());
                    newOffering.setTeacherId(templateOffering.getTeacherId());
                    newOffering.setAcademicYear(templateOffering.getAcademicYear());
                    newOffering.setSemester(templateOffering.getSemester());
                    newOffering.setCapacity(templateOffering.getCapacity());
                    courseOfferingMapper.insert(newOffering);
                    log.info("为课程 '{}' 克隆了新的待排课实例，ID: {}", catalog.getName(), newOffering.getId());

                    // 2. 克隆班级关联信息
                    if (templateOffering.getAssociatedClasses() != null && !templateOffering.getAssociatedClasses().isEmpty()) {
                        for (CourseOffering.ClassInfo classInfo : templateOffering.getAssociatedClasses()) {
                            OfferingClassLink newLink = new OfferingClassLink();
                            newLink.setCourseOfferingId(newOffering.getId());
                            newLink.setClassId(classInfo.getClassId());
                            newLink.setCourseType(classInfo.getCourseType());
                            offeringClassLinkMapper.insert(newLink);
                        }
                    }

                    // 3. 将新创建的、信息完整的课程安排加入待排课列表
                    offeringsToSchedule.add(courseOfferingMapper.findById(newOffering.getId()));
                }
            } else if (currentCount == 0 && requiredCount > 0) {
                log.warn("课程 '{}' (ID: {}) 需要安排 {} 节课，但未找到任何可供克隆的模板课程安排。请至少手动为其创建一个完整的课程安排（包括指定教师和班级）。", catalog.getName(), catalog.getId(), requiredCount);
            }
        }

        if (offeringsToSchedule.isEmpty()) {
            log.warn("在当前学年学期下没有找到任何需要排课的课程。任务终止。");
            return;
        }

        if (availableClassrooms.isEmpty()) {
            log.warn("没有可用的教室，无法进行排课。任务终止。");
            return;
        }

        log.info("数据准备完毕。共需安排 {} 个课时。正在构建方案空间...", offeringsToSchedule.size());

        final List<List<ScheduledUnit>> scheduleSpace = new ArrayList<>();
        for (CourseOffering offering : offeringsToSchedule) {
            List<ScheduledUnit> possibleUnitsForCourse = new ArrayList<>();
            for (int day = 1; day <= 5; day++) {
                for (int time = 1; time <= 5; time++) {
                    for (Classroom classroom : availableClassrooms) {
                        possibleUnitsForCourse.add(new ScheduledUnit(offering, day, time, classroom));
                    }
                }
            }
            scheduleSpace.add(possibleUnitsForCourse);
        }

        if (scheduleSpace.stream().anyMatch(List::isEmpty)) {
            log.error("构建方案空间失败，存在无法安排的课程（可能因为没有可用教室）。任务终止。");
            return;
        }

        List<IntegerChromosome> chromosomes = new ArrayList<>();
        for (int i = 0; i < offeringsToSchedule.size(); i++) {
            chromosomes.add(IntegerChromosome.of(0, scheduleSpace.get(i).size() - 1));
        }
        Factory<Genotype<IntegerGene>> gtf = Genotype.of(chromosomes);

        log.info("开始配置遗传算法引擎...");
        Engine<IntegerGene, Integer> engine = Engine.builder(
                        genotype -> calculateFitness(genotype, scheduleSpace),
                        gtf
                )
                .populationSize(POPULATION_SIZE)
                .survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new RouletteWheelSelector<>())
                .alterers(
                        new SwapMutator<>(MUTATION_PROBABILITY),
                        new SinglePointCrossover<>(CROSSOVER_PROBABILITY)
                )
                .build();
        log.info("引擎配置完毕，开始进化计算...");
        Phenotype<IntegerGene, Integer> bestPhenotype = engine.stream()
                .limit(Limits.bySteadyFitness(MAX_NO_IMPROVEMENT_GENERATIONS))
                .limit(MAX_GENERATIONS)
                .collect(EvolutionResult.toBestPhenotype());
        log.info("进化计算完成。");
        Genotype<IntegerGene> bestGenotype = bestPhenotype.genotype();
        log.info("找到最优解，惩罚分数为: {}. 正在保存至数据库...", -bestPhenotype.fitness());

        for (int i = 0; i < bestGenotype.chromosome().length(); i++) {
            int geneValue = bestGenotype.chromosome().get(i).allele();
            ScheduledUnit bestUnit = scheduleSpace.get(i).get(geneValue);
            courseOfferingMapper.updateSchedule(
                    bestUnit.offering().getId(),
                    bestUnit.dayOfWeek(),
                    bestUnit.timeSlot(),
                    bestUnit.classroom().getId()
            );
        }

        long endTime = System.currentTimeMillis();
        log.info("【排课任务成功】总耗时: {} ms", (endTime - startTime));
    }
}