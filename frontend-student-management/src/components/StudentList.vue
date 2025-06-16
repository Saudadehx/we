<template>
  <div class="list-container">
    <div v-for="(classes, grade) in groupedStudents" :key="grade" class="grade-group">
      <h4 class="grade-title">{{ grade }}</h4>
      <div v-for="(studentList, className) in classes" :key="className" class="class-group">
        <p class="class-title">{{ className }}</p>
        <div
            v-for="student in studentList"
            :key="student.id"
            class="student-item"
            @click="selectStudent(student)"
        >
          <img :src="student.photoUrl || 'default-avatar.png'" alt="avatar" class="avatar">
          <div class="student-info">
            <strong>{{ student.name }}</strong>
            <span>{{ student.studentId }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  students: {
    type: Array,
    required: true
  }
});

const emit = defineEmits(['student-selected']);

// 使用计算属性来动态分组学生数据
const groupedStudents = computed(() => {
  const groups = {};
  for (const student of props.students) {
    // 假设班级名称格式为 "23数据1班"，从中提取年级 "23级"
    const gradeMatch = student.className.match(/^\d+/);
    const grade = gradeMatch ? `${gradeMatch[0]}级` : '未分类';
    const className = student.className || '未分配班级';

    if (!groups[grade]) {
      groups[grade] = {};
    }
    if (!groups[grade][className]) {
      groups[grade][className] = [];
    }
    groups[grade][className].push(student);
  }
  return groups;
});

const selectStudent = (student) => {
  emit('student-selected', student);
};
</script>

<style scoped>
.list-container {
  padding: 10px;
}
.grade-group {
  margin-bottom: 15px;
}
.grade-title {
  font-size: 1.1em;
  color: #333;
  padding-left: 5px;
  border-bottom: 1px solid #ccc;
  margin-bottom: 5px;
}
.class-title {
  font-size: 0.95em;
  color: #555;
  padding-left: 15px;
  font-weight: bold;
}
.student-item {
  display: flex;
  align-items: center;
  padding: 8px 10px 8px 25px; /* 增加左侧内边距以体现层级 */
  cursor: pointer;
  border-radius: 4px;
}
.student-item:hover {
  background-color: #e9ecef;
}
.avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  margin-right: 10px;
}
.student-info {
  display: flex;
  flex-direction: column;
}
.student-info strong {
  font-weight: 600;
}
.student-info span {
  font-size: 0.9em;
  color: #666;
}
</style>