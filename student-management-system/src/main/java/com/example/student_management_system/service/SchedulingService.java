// file: student-management-system/src/main/java/com/example/student_management_system/service/SchedulingService.java
// FINAL CORRECTED VERSION

package com.example.student_management_system.service;

import com.example.student_management_system.mapper.ClassroomMapper;
import com.example.student_management_system.mapper.CourseOfferingMapper;
import com.example.student_management_system.model.Classroom;
import com.example.student_management_system.model.CourseOffering;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.SinglePointCrossover;
import io.jenetics.SwapMutator;
import io.jenetics.TournamentSelector;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class SchedulingService {

    // --- 算法参数 ---
    private static final int POPULATION_SIZE = 200;
    private static final double MUTATION_PROBABILITY = 0.05;
    private static final double CROSSOVER_PROBABILITY = 0.3;
    private static final int MAX_GENERATIONS = 300;
    private static final int MAX_NO_IMPROVEMENT_GENERATIONS = 50;

    // --- 惩罚权重 ---
    private static final int HARD_CONSTRAINT_PENALTY = 1000;
    private static final int SOFT_CONSTRAINT_PENALTY = 1;


    private final CourseOfferingMapper courseOfferingMapper;
    private final ClassroomMapper classroomMapper;

    @Autowired
    public SchedulingService(CourseOfferingMapper courseOfferingMapper, ClassroomMapper classroomMapper) {
        this.courseOfferingMapper = courseOfferingMapper;
        this.classroomMapper = classroomMapper;
    }

    // 代表一个具体的排课单元
    public record ScheduledUnit(CourseOffering offering, Integer dayOfWeek, Integer timeSlot, Classroom classroom) {}

    // 【核心修正】适应度函数现在接收一个整数基因型
    private int calculateFitness(final Genotype<IntegerGene> genotype, final List<List<ScheduledUnit>> scheduleSpace) {

        // --- 解码步骤 ---
        // 将整数基因解码为一张可读的课程表
        List<ScheduledUnit> currentSchedule = new ArrayList<>();
        for (int i = 0; i < genotype.chromosome().length(); i++) {
            int geneValue = genotype.chromosome().get(i).allele(); // 获取第i门课的基因值(索引)
            currentSchedule.add(scheduleSpace.get(i).get(geneValue)); // 从方案空间中查找到对应的安排
        }

        int penalty = 0;

        // ---- 硬约束检查 (逻辑不变, 操作对象变为解码后的 currentSchedule) ----
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
                .filter(unit -> unit.offering().getAssociatedClasses() != null)
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

        // ---- 软约束检查 (逻辑不变) ----
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
        List<CourseOffering> offeringsToSchedule = courseOfferingMapper.findAllWithDetails();
        List<Classroom> availableClassrooms = classroomMapper.findAll();

        if (offeringsToSchedule.isEmpty() || availableClassrooms.isEmpty()) {
            log.warn("没有需要排课的课程或没有可用的教室，任务终止。");
            return;
        }
        log.info("数据加载完毕。共需安排 {} 门课程，有 {} 间教室可用。", offeringsToSchedule.size(), availableClassrooms.size());

        // --- 【核心修正】步骤 1: 构建方案空间 (一个查找表) ---
        final List<List<ScheduledUnit>> scheduleSpace = new ArrayList<>();
        for (CourseOffering offering : offeringsToSchedule) {
            List<ScheduledUnit> possibleUnitsForCourse = new ArrayList<>();
            for (int day = 1; day <= 5; day++) { // 周一到周五
                for (int time = 1; time <= 5; time++) { // 每天5个时间段
                    for (Classroom classroom : availableClassrooms) {
                        possibleUnitsForCourse.add(new ScheduledUnit(offering, day, time, classroom));
                    }
                }
            }
            scheduleSpace.add(possibleUnitsForCourse);
        }

        // --- 【核心修正】步骤 2: 创建基因工厂 (Factory for IntegerChromosome) ---
        List<IntegerChromosome> chromosomes = new ArrayList<>();
        for (int i = 0; i < offeringsToSchedule.size(); i++) {
            // 第 i 个基因的取值范围是 [0, scheduleSpace.get(i).size() - 1]
            chromosomes.add(IntegerChromosome.of(0, scheduleSpace.get(i).size() - 1));
        }
        Factory<Genotype<IntegerGene>> gtf = Genotype.of(chromosomes);


        // --- 【核心修正】步骤 3: 构建引擎 ---
        log.info("开始配置遗传算法引擎...");
        Engine<IntegerGene, Integer> engine = Engine.builder(
                        genotype -> calculateFitness(genotype, scheduleSpace), // 适应度函数现在需要传入方案空间
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

        // --- 【核心修正】步骤 4: 解码最优基因并保存结果 ---
        Genotype<IntegerGene> bestGenotype = bestPhenotype.genotype();
        log.info("找到最优解，惩罚分数为: {}. 正在保存至数据库...", -bestPhenotype.fitness());

        for (int i = 0; i < bestGenotype.chromosome().length(); i++) {
            int geneValue = bestGenotype.chromosome().get(i).allele();
            ScheduledUnit bestUnit = scheduleSpace.get(i).get(geneValue);

            CourseOffering offeringToUpdate = bestUnit.offering();
            offeringToUpdate.setCourseDay(bestUnit.dayOfWeek());
            offeringToUpdate.setCourseTime(bestUnit.timeSlot());
            offeringToUpdate.setClassroomId(bestUnit.classroom().getId());
            courseOfferingMapper.update(offeringToUpdate);
        }

        long endTime = System.currentTimeMillis();
        log.info("【排课任务成功】总耗时: {} ms", (endTime - startTime));
    }
}