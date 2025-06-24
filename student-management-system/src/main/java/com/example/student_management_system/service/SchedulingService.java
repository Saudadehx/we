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

    public record ScheduledUnit(CourseOffering offering, Integer dayOfWeek, Integer timeSlot, Classroom classroom) {
    }

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


    // 这是最终的、经过两次修正后正确的版本

    // 这是修复了班级关联复制问题的最终版本

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
        log.info("当前排课目标周期: 第 {} 学年, 第 {} 学期", currentAcademicYear, currentSemester);

        if (allCatalogs.isEmpty()) {
            log.warn("课程目录为空，任务终止。");
            return;
        }

        // --- 第一步：按需补全课程实例 ---
        for (CourseCatalog catalog : allCatalogs) {
            int requiredCount = catalog.getLessonsPerWeek() != null ? catalog.getLessonsPerWeek() : 1;

            long currentCountInTargetSemester = courseOfferingMapper.findByCourseCatalogId(catalog.getId())
                    .stream()
                    .filter(o -> Objects.equals(o.getAcademicYear(), currentAcademicYear) && Objects.equals(o.getSemester(), currentSemester))
                    .count();

            int neededCount = requiredCount - (int) currentCountInTargetSemester;

            if (neededCount > 0) {
                CourseOffering templateOffering = courseOfferingMapper.findByCourseCatalogId(catalog.getId())
                        .stream()
                        .findFirst()
                        .map(o -> courseOfferingMapper.findById(o.getId())) // 确保加载完整信息
                        .orElse(null);

                if (templateOffering != null) {
                    log.info("课程 '{}' 需要 {} 节课, 当前有 {} 节, 准备补足 {} 节.", catalog.getName(), requiredCount, currentCountInTargetSemester, neededCount);
                    for (int i = 0; i < neededCount; i++) {
                        CourseOffering newOffering = new CourseOffering();
                        newOffering.setCourseCatalogId(templateOffering.getCourseCatalogId());
                        newOffering.setTeacherId(templateOffering.getTeacherId());
                        newOffering.setCapacity(templateOffering.getCapacity());
                        newOffering.setAcademicYear(currentAcademicYear);
                        newOffering.setSemester(currentSemester);
                        courseOfferingMapper.insert(newOffering); // 插入以获取ID

                        // 【核心修正】确保班级关联信息被完整克隆
                        if (templateOffering.getAssociatedClasses() != null && !templateOffering.getAssociatedClasses().isEmpty()) {
                            for (CourseOffering.ClassInfo classInfo : templateOffering.getAssociatedClasses()) {
                                OfferingClassLink newLink = new OfferingClassLink();
                                newLink.setCourseOfferingId(newOffering.getId());
                                newLink.setClassId(classInfo.getClassId());
                                newLink.setCourseType(classInfo.getCourseType());
                                offeringClassLinkMapper.insert(newLink);
                            }
                            log.info("已为新课程实例 (ID: {}) 克隆了 {} 条班级关联。", newOffering.getId(), templateOffering.getAssociatedClasses().size());
                        } else {
                            log.warn("模板课程 (ID: {}) 没有班级关联信息，新克隆的实例 (ID: {}) 也将没有班级关联。", templateOffering.getId(), newOffering.getId());
                        }
                    }
                } else {
                    log.warn("课程 '{}' 无法补足实例，因为在整个系统中都找不到可供参考的模板。", catalog.getName());
                }
            }
        }

        // --- 第二步：只收集当前学期内“未被安排”的课程 ---
        List<CourseOffering> offeringsToSchedule = courseOfferingMapper.findAllWithDetails().stream()
                .filter(o -> Objects.equals(o.getAcademicYear(), currentAcademicYear) &&
                        Objects.equals(o.getSemester(), currentSemester) &&
                        (o.getCourseDay() == null || o.getCourseTime() == null || o.getClassroomId() == null))
                .collect(Collectors.toList());

        if (offeringsToSchedule.isEmpty()) {
            log.info("当前目标周期内没有需要安排（时间、教室待定）的课程。任务完成。");
            return;
        }
        if (availableClassrooms.isEmpty()) {
            log.warn("没有可用的教室，无法进行排课。任务终止。");
            return;
        }
        log.info("数据准备完毕。共需安排 {} 个课时。正在构建方案空间...", offeringsToSchedule.size());

        // --- 第三步：构建方案空间 ---
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
            log.error("构建方案空间失败，存在无法安排的课程。任务终止。");
            return;
        }

        // --- 第四步：正确构建基因型 ---
        log.info("正在构建遗传算法基因型...");
        List<IntegerChromosome> chromosomes = new ArrayList<>();
        for (int i = 0; i < offeringsToSchedule.size(); i++) {
            chromosomes.add(IntegerChromosome.of(0, scheduleSpace.get(i).size() - 1));
        }
        Factory<Genotype<IntegerGene>> gtf = Genotype.of(chromosomes);

        // --- 第五步：运行遗传算法引擎 ---
        // (此部分保持不变)
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