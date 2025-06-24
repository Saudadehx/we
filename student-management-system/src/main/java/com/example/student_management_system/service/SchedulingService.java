package com.example.student_management_system.service;

import com.example.student_management_system.mapper.ClassroomMapper;
import com.example.student_management_system.mapper.CourseCatalogMapper;
import com.example.student_management_system.mapper.CourseOfferingMapper;
import com.example.student_management_system.mapper.OfferingClassLinkMapper;
import com.example.student_management_system.model.Classroom;
import com.example.student_management_system.model.CourseCatalog;
import com.example.student_management_system.model.CourseOffering;
import com.example.student_management_system.model.OfferingClassLink;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    // 【新增】为新的软性约束定义一个惩罚值
    private static final int DISTRIBUTION_PENALTY = 10;

    private final CourseOfferingMapper courseOfferingMapper;
    private final ClassroomMapper classroomMapper;
    private final CourseCatalogMapper courseCatalogMapper;
    private final SystemSettingService systemSettingService;
    private final OfferingClassLinkMapper offeringClassLinkMapper;

    private final SchedulingService self;

    @Autowired
    public SchedulingService(CourseOfferingMapper courseOfferingMapper, ClassroomMapper classroomMapper,
                             CourseCatalogMapper courseCatalogMapper, SystemSettingService systemSettingService,
                             OfferingClassLinkMapper offeringClassLinkMapper, @Lazy SchedulingService self) {
        this.courseOfferingMapper = courseOfferingMapper;
        this.classroomMapper = classroomMapper;
        this.courseCatalogMapper = courseCatalogMapper;
        this.systemSettingService = systemSettingService;
        this.offeringClassLinkMapper = offeringClassLinkMapper;
        this.self = self;
    }

    public record ScheduledUnit(CourseOffering offering, Integer dayOfWeek, Integer timeSlot, Classroom classroom) {
    }

    /**
     * 适应度函数（排课质量检查员）
     */
    private int calculateFitness(final Genotype<IntegerGene> genotype, final List<List<ScheduledUnit>> scheduleSpace) {
        final List<ScheduledUnit> currentSchedule = new ArrayList<>();

        // 【最终重要修正】正确地从基因型(genotype)构建完整的课表方案(currentSchedule)
        // 这个循环现在会遍历每一条染色体，构建一个包含所有待排课程的完整列表
        int offeringIndex = 0;
        for (Chromosome<IntegerGene> chromosome : genotype) {
            int geneValue = chromosome.gene().allele();
            currentSchedule.add(scheduleSpace.get(offeringIndex).get(geneValue));
            offeringIndex++;
        }

        int penalty = 0;

        // --- 硬性约束惩罚 (Hard Constraints) ---

        // 1. 教师冲突检查
        Map<String, Long> teacherSchedule = currentSchedule.stream()
                .filter(unit -> unit.offering().getTeacherId() != null && unit.dayOfWeek() != null && unit.timeSlot() != null)
                .collect(Collectors.groupingBy(
                        unit -> unit.offering().getTeacherId() + "-" + unit.dayOfWeek() + "-" + unit.timeSlot(),
                        Collectors.counting()
                ));
        penalty += teacherSchedule.values().stream().mapToInt(count -> (int) (count > 1 ? (count - 1) * HARD_CONSTRAINT_PENALTY : 0)).sum();

        // 2. 教室冲突检查
        Map<String, Long> classroomSchedule = currentSchedule.stream()
                .filter(unit -> unit.classroom() != null && unit.dayOfWeek() != null && unit.timeSlot() != null)
                .collect(Collectors.groupingBy(
                        unit -> unit.classroom().getId() + "-" + unit.dayOfWeek() + "-" + unit.timeSlot(),
                        Collectors.counting()
                ));
        penalty += classroomSchedule.values().stream().mapToInt(count -> (int) (count > 1 ? (count - 1) * HARD_CONSTRAINT_PENALTY : 0)).sum();

        // 3. 班级冲突检查
        Map<Long, List<ScheduledUnit>> classSchedules = currentSchedule.stream()
                .filter(unit -> unit.offering().getAssociatedClasses() != null && !unit.offering().getAssociatedClasses().isEmpty())
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

        // --- 软性约束惩罚 (Soft Constraints) ---

        // 4. 课程容量 > 教室容量
        for (ScheduledUnit unit : currentSchedule) {
            if (unit.offering().getCapacity() != null && unit.classroom() != null) {
                if (unit.offering().getCapacity() > unit.classroom().getCapacity()) {
                    penalty += (unit.offering().getCapacity() - unit.classroom().getCapacity()) * SOFT_CONSTRAINT_PENALTY;
                }
            }
        }

        // 5. 同一门课尽量分散在不同天
        Map<String, Long> courseSessionsOnSameDay = currentSchedule.stream()
                .filter(unit -> unit.offering().getCourseCatalogId() != null && unit.dayOfWeek() != null)
                .collect(Collectors.groupingBy(
                        unit -> unit.offering().getCourseCatalogId() + "-" + unit.dayOfWeek(),
                        Collectors.counting()
                ));
        penalty += courseSessionsOnSameDay.values().stream()
                .mapToInt(count -> (int) (count > 1 ? (count - 1) * DISTRIBUTION_PENALTY : 0))
                .sum();

        return -penalty;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSingleSchedule(ScheduledUnit unit) {
        log.debug("正在为课程安排ID: {} 保存排课结果...", unit.offering().getId());
        courseOfferingMapper.updateSchedule(
                unit.offering().getId(),
                unit.dayOfWeek(),
                unit.timeSlot(),
                unit.classroom().getId()
        );
        log.debug("课程安排ID: {} 的排课结果保存成功。", unit.offering().getId());
    }

    @Async
    @Transactional
    public void generateSchedule() {
        log.info("【排课任务开始】");
        long startTime = System.currentTimeMillis();

        log.info("正在加载排课所需的基础数据...");
        List<Classroom> availableClassrooms = classroomMapper.findAll();
        int currentAcademicYear = systemSettingService.getCurrentAcademicYear();
        int currentSemester = systemSettingService.getCurrentSemester();
        log.info("当前排课目标周期: 第 {} 学年, 第 {} 学期", currentAcademicYear, currentSemester);

        if (availableClassrooms.isEmpty()) {
            log.warn("没有可用的教室，无法进行排课。任务终止。");
            return;
        }

        List<CourseOffering> offeringsToSchedule = courseOfferingMapper.findAllWithDetails().stream()
                .filter(o -> Objects.equals(o.getAcademicYear(), currentAcademicYear) &&
                        Objects.equals(o.getSemester(), currentSemester) &&
                        (o.getCourseDay() == null || o.getCourseTime() == null || o.getClassroomId() == null))
                .collect(Collectors.toList());

        if (offeringsToSchedule.isEmpty()) {
            log.info("当前目标周期内没有需要安排（时间、教室待定）的课程。任务完成。");
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
            log.error("构建方案空间失败，存在无法安排的课程。任务终止。");
            return;
        }

        log.info("正在构建遗传算法基因型...");
        List<IntegerChromosome> chromosomes = new ArrayList<>();
        for (int i = 0; i < offeringsToSchedule.size(); i++) {
            chromosomes.add(IntegerChromosome.of(0, scheduleSpace.get(i).size() - 1));
        }
        Factory<Genotype<IntegerGene>> gtf = Genotype.of(chromosomes);

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

        int offeringIndex = 0;
        for (Chromosome<IntegerGene> chromosome : bestGenotype) {
            int geneValue = chromosome.gene().allele();
            ScheduledUnit bestUnit = scheduleSpace.get(offeringIndex).get(geneValue);
            self.saveSingleSchedule(bestUnit);
            offeringIndex++;
        }

        long endTime = System.currentTimeMillis();
        log.info("【排课任务成功】总耗时: {} ms", (endTime - startTime));
    }
}