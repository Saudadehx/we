<template>
  <div class="list-container">
    <div class="list-header">
      <h4>学生列表</h4>
      <button v-if="isAdmin" @click="requestAddStudent" class="add-btn-text">新增学生</button>
    </div>

    <div class="student-groups">
      <div v-for="(classes, grade) in groupedStudents" :key="grade" class="grade-group">
        <div class="group-header grade-header" @click="toggleGrade(grade)">
          <span class="arrow" :class="{ expanded: isGradeExpanded(grade) }"></span>
          {{ grade }}
        </div>

        <div v-if="isGradeExpanded(grade)" class="class-list">
          <div v-for="(studentList, className) in classes" :key="className" class="class-group">
            <div class="group-header class-header" @click="toggleClasse(grade, className)">
              <span class="arrow" :class="{ expanded: isClassExpanded(grade, className) }"></span>
              {{ className }}
            </div>

            <TransitionGroup name="student-list-anim">
              <div v-if="isClassExpanded(grade, className)">
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
            </TransitionGroup>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { authService } from '@/services/apiService';

const props = defineProps({
  students: {
    type: Array,
    required: true
  }
});

const emit = defineEmits(['student-selected', 'add-student-requested']);

const isAdmin = computed(() => authService.getUserRole() === 'ADMIN');

// === 折叠状态管理 ===
const expandedState = ref({}); // 存储展开状态，例如 { '23级': true, '23级_23数据1班': false }

const toggleGrade = (grade) => {
  expandedState.value[grade] = !expandedState.value[grade];
};

const toggleClasse = (grade, className) => {
  const key = `${grade}_${className}`;
  expandedState.value[key] = !expandedState.value[key];
};

// 默认展开所有年级和班级
const setDefaultExpanded = () => {
  for (const grade in groupedStudents.value) {
    expandedState.value[grade] = true;
    for (const className in groupedStudents.value[grade]) {
      const key = `${grade}_${className}`;
      expandedState.value[key] = true;
    }
  }
}

const isGradeExpanded = (grade) => !!expandedState.value[grade];
const isClassExpanded = (grade, className) => !!expandedState.value[`${grade}_${className}`];


const groupedStudents = computed(() => {
  const groups = {};
  for (const student of props.students) {
    const gradeMatch = student.className.match(/^\d+/);
    const grade = gradeMatch ? `${gradeMatch[0]}级` : '未分类';
    const className = student.className || '未分配班级';

    if (!groups[grade]) groups[grade] = {};
    if (!groups[grade][className]) groups[grade][className] = [];
    groups[grade][className].push(student);
  }
  // 数据分组后，设置默认展开状态
  setDefaultExpanded();
  return groups;
});

const selectStudent = (student) => {
  emit('student-selected', student);
};

const requestAddStudent = () => {
  emit('add-student-requested');
};
</script>

<style scoped>
/* 整体容器和头部 */
.list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  user-select: none; /* 标准浏览器 */
  -webkit-user-select: none; /* 兼容 Chrome, Safari, Opera */
  -moz-user-select: none; /* 兼容 Firefox */
  -ms-user-select: none;
}
/* 在 StudentList.vue 的 <style scoped> 中 */

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #e0e0e0;
  flex-shrink: 0;
}
.list-header h4 {
  margin: 0;
  font-size: 1.1em;
}

.add-btn-text {
  background-color: #198754;
  color: white;
  border: none;
  padding: 3px 12px;
  border-radius: 6px;
  font-size: 0.9em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.add-btn-text:hover {
  opacity: 0.9;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 分组样式 */
.student-groups {
  overflow-y: auto;
  flex-grow: 1;
  padding: 10px;
}
.group-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}
.group-header:hover {
  background-color: #f0f0f0;
}
.grade-header {
  font-size: 1.05em;
  font-weight: bold;
  color: #333;
}
.class-header {
  font-size: 0.95em;
  padding-left: 25px; /* 班级缩进 */
  color: #555;
}
.arrow {
  display: inline-block;
  width: 0;
  height: 0;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 6px solid #666;
  margin-right: 10px;
  transition: transform 0.3s ease;
}
.arrow.expanded {
  transform: rotate(90deg);
}

/* 学生列表项 */
.student-item {
  display: flex;
  align-items: center;
  padding: 3px 10px 3px 30px; /* 学生项进一步缩进 */
  cursor: pointer;
  border-radius: 4px;
}
.student-item:hover {
  background-color: #e9ecef;
}
.avatar {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  margin-right: 10px;
}
.student-info {
  display: flex;
  flex-direction: column;
}

/* 折叠动画 */
.student-list-anim-enter-active,
.student-list-anim-leave-active {
  transition: all 0.4s ease;
}
.student-list-anim-enter-from,
.student-list-anim-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>