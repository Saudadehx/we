<template>
  <div class="list-container">
    <div class="list-header">
      <h4>学生列表</h4>
      <button v-if="isAdmin" @click="requestAddStudent" class="add-btn">新增</button>
    </div>

    <div class="student-groups">
      <div v-for="(group, grade) in groupedStudents" :key="grade" class="grade-group">
        <div class="group-header grade-header" @click="toggle(grade)">
          <span class="arrow" :class="{ expanded: expandedState[grade] }"></span>
          {{ grade }}
        </div>
        <div v-show="expandedState[grade]" class="class-list">
          <div v-for="(studentList, className) in group" :key="className" class="class-group">
            <div class="group-header class-header" @click="toggle(className)">
              <span class="arrow" :class="{ expanded: expandedState[className] }"></span>
              {{ className }}
            </div>
            <ul v-show="expandedState[className]" class="student-ul">
              <li
                  v-for="student in studentList"
                  :key="student.id"
                  class="student-item"
                  @click="selectStudent(student)"
                  :class="{ active: selectedStudentId === student.id }"
              >
                <div class="student-info">
                  <strong>{{ student.name }}</strong>
                  <span>{{ student.studentId }}</span>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script部分略有修改，以支持active状态
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';

const props = defineProps({
  students: { type: Array, required: true },
  // 假设从父组件传入当前选中的学生ID
  selectedStudentId: { type: [Number, null], default: null }
});
const emit = defineEmits(['student-selected', 'add-student-requested']);
const authStore = useAuthStore();
const isAdmin = computed(() => authStore.isAdmin);
const expandedState = ref({});

const groupedStudents = computed(() => {
  const groups = {};
  if (!props.students) return {};
  props.students.forEach(student => {
    if (!student.className) return;
    const gradeMatch = student.className.match(/^\d+/);
    const grade = gradeMatch ? `${gradeMatch[0]}级` : '未分类';
    if (!groups[grade]) groups[grade] = {};
    if (!groups[grade][student.className]) groups[grade][student.className] = [];
    groups[grade][student.className].push(student);
  });
  return groups;
});

watch(groupedStudents, (newGroups) => {
  const newExpandedState = {};
  for (const grade in newGroups) {
    newExpandedState[grade] = true;
    for (const className in newGroups[grade]) {
      newExpandedState[className] = true;
    }
  }
  expandedState.value = newExpandedState;
}, { deep: true, immediate: true });

const toggle = (key) => { expandedState.value[key] = !expandedState.value[key]; };
const selectStudent = (student) => { emit('student-selected', student); };
const requestAddStudent = () => { emit('add-student-requested'); };
</script>

<style scoped>
.list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  user-select: none;
  background-color: var(--color-surface);
  border-right: 1px solid var(--color-border);
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--spacing-md);
  height: 64px;
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}
.list-header h4 {
  margin: 0;
  font-size: 1.1em;
}
.add-btn {
  background-color: var(--color-primary);
  color: white;
  border: none;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  font-size: 0.9em;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-speed) ease;
}
.add-btn:hover {
  opacity: 0.9;
}

.student-groups {
  overflow-y: auto;
  flex-grow: 1;
  padding: var(--spacing-md);
}
.group-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  transition: background-color var(--transition-speed) ease;
  font-weight: 600;
}
.group-header:hover {
  background-color: var(--color-background);
}
.grade-header { font-size: 1.05em; }
.class-header {
  font-size: 0.95em;
  padding-left: var(--spacing-lg); /* 增加缩进 */
  color: var(--color-text-secondary);
}
.arrow {
  display: inline-block;
  width: 16px;
  height: 16px;
  margin-right: var(--spacing-sm);
  transition: transform var(--transition-speed) ease;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M5 2l6 6-6 6'/%3e%3c/svg%3e");
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  transform: rotate(90deg);
}
.arrow:not(.expanded) {
  transform: rotate(0deg);
}
.student-ul {
  padding-left: calc(var(--spacing-lg) + 16px);
  list-style: none;
  margin: 0;
}

.student-item {
  padding: var(--spacing-sm) var(--spacing-md);
  cursor: pointer;
  border-radius: var(--border-radius);
  transition: background-color var(--transition-speed) ease;
}
.student-item:hover {
  background-color: var(--color-background);
}
.student-item.active {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
}
.student-item.active .student-info strong {
  color: var(--color-primary);
}
.student-item.active .student-info span {
  color: var(--color-primary);
}

.student-info { display: flex; flex-direction: column; }
.student-info strong {
  font-size: 0.95em;
  font-weight: 500;
  color: var(--color-text-primary);
}
.student-info span {
  font-size: 0.8em;
  color: var(--color-text-secondary);
}
</style>