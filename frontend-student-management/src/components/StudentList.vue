<template>
  <div class="list-container">
    <div class="list-header">
      <h4>学生列表</h4>
      <button v-if="isAdmin" @click="requestAddStudent" class="add-btn-text">新增学生</button>
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
            <div v-show="expandedState[className]">
              <div
                  v-for="student in studentList"
                  :key="student.id"
                  class="student-item"
                  @click="selectStudent(student)"
              >
                <div class="student-info">
                  <strong>{{ student.name }}</strong>
                  <span>{{ student.studentId }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script 部分使用我们最终调试好的版本，无需修改
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';

const props = defineProps({ students: { type: Array, required: true } });
const emit = defineEmits(['student-selected', 'add-student-requested']);
const authStore = useAuthStore();
const isAdmin = computed(() => authStore.isAdmin);
const expandedState = ref({});

const groupedStudents = computed(() => {
  const groups = {};
  if (!props.students) return {};
  for (const student of props.students) {
    if (!student.className) continue;
    const gradeMatch = student.className.match(/^\d+/);
    const grade = gradeMatch ? `${gradeMatch[0]}级` : '未分类';
    if (!groups[grade]) groups[grade] = {};
    if (!groups[grade][student.className]) groups[grade][student.className] = [];
    groups[grade][student.className].push(student);
  }
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
.list-container { display: flex; flex-direction: column; height: 100%; user-select: none; }
.list-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 15px; border-bottom: 1px solid #e0e0e0; flex-shrink: 0; }
.list-header h4 { margin: 0; font-size: 1.1em; }
.add-btn-text { background-color: #198754; color: white; border: none; padding: 3px 12px; border-radius: 6px; font-size: 0.9em; font-weight: 500; cursor: pointer; transition: all 0.2s; }
.add-btn-text:hover { opacity: 0.9; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.student-groups { overflow-y: auto; flex-grow: 1; padding: 10px; }
.group-header { display: flex; align-items: center; cursor: pointer; padding: 8px; border-radius: 4px; transition: background-color 0.2s; }
.group-header:hover { background-color: #f0f0f0; }
.grade-header { font-size: 1.05em; font-weight: bold; }
.class-header { font-size: 0.95em; padding-left: 25px; }
.arrow { display: inline-block; width: 0; height: 0; border-top: 5px solid transparent; border-bottom: 5px solid transparent; border-left: 6px solid #666; margin-right: 10px; transition: transform 0.3s ease; }
.arrow.expanded { transform: rotate(90deg); }
.student-item { display: flex; align-items: center; padding: 5px 10px 5px 50px; cursor: pointer; border-radius: 4px; }
.student-item:hover { background-color: #e9ecef; }
.student-info { display: flex; flex-direction: column; }
.student-info strong { font-size: 0.95em; }
.student-info span { font-size: 0.8em; color: #777; }

/* ✨ 优化点：隐藏滚动条样式 */
.student-groups::-webkit-scrollbar { width: 5px; }
.student-groups::-webkit-scrollbar-track { background: transparent; }
.student-groups::-webkit-scrollbar-thumb { background: #ccc; border-radius: 5px; }
.student-groups::-webkit-scrollbar-thumb:hover { background: #aaa; }
</style>