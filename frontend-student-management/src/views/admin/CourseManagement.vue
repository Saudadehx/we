<template>
  <div class="page-container">
    <header class="page-header">
      <h1>课程安排管理</h1>
      <button @click="openAddModal({})" class="add-btn">新增课程安排</button>
    </header>

    <div class="content-card">
      <ScheduleGrid
          :schedules="schedules"
          :majors="majors"
          @cell-click="handleCellClick"
          @course-click="handleCourseClick"
          v-if="!isLoading"
      />
      <div v-else>正在加载课表...</div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content" style="width: 500px;">
        <h2>{{ isEditing ? '编辑课程安排' : '新增课程安排' }}</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-grid">
            <div class="form-group">
              <label for="courseId">基础课程</label>
              <select v-model="editableSchedule.courseId" id="courseId" required>
                <option disabled value="">请选择基础课程</option>
                <option v-for="course in courses" :key="course.id" :value="course.id">
                  {{ course.courseName }} ({{ course.courseId }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="teacherId">授课教师</label>
              <select v-model="editableSchedule.teacherId" id="teacherId" required>
                <option disabled value="">请选择教师</option>
                <option v-for="teacher in teachers" :key="teacher.id" :value="teacher.id">
                  {{ teacher.name }} ({{ teacher.teacherId }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="majorId">所属专业 (必修课)</label>
              <select v-model="editableSchedule.majorId" id="majorId">
                <option :value="null">通用选修课或未指定</option>
                <option v-for="major in majors" :key="major.id" :value="major.id">{{ major.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label for="academicYear">开设学年</label>
              <select v-model="editableSchedule.academicYear" id="academicYear" required>
                <option disabled value="">请选择</option>
                <option v-for="n in 4" :key="n" :value="n">第 {{ n }} 学年</option>
              </select>
            </div>
            <div class="form-group">
              <label for="semester">开设学期</label>
              <select v-model="editableSchedule.semester" id="semester" required>
                <option disabled value="">请选择</option>
                <option value="1">上学期</option>
                <option value="2">下学期</option>
              </select>
            </div>
            <div class="form-group">
              <label for="courseDay">上课日</label>
              <select v-model="editableSchedule.courseDay" id="courseDay">
                <option :value="null">未安排</option>
                <option v-for="day in 7" :key="day" :value="day">星期{{ '一二三四五六日'[day-1] }}</option>
              </select>
            </div>
            <div class="form-group">
              <label for="courseTime">上课时段</label>
              <select v-model="editableSchedule.courseTime" id="courseTime">
                <option :value="null">未安排</option>
                <option v-for="time in 5" :key="time" :value="time">第 {{ time }} 大节</option>
              </select>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" @click="handleDelete" class="delete-btn" v-if="isEditing">删除</button>
            <div style="flex-grow: 1;"></div>
            <button type="button" @click="closeModal">取消</button>
            <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '处理中...' : '提交' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { courseService, teacherService, majorService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';
import ScheduleGrid from '@/components/ScheduleGrid.vue';

const schedules = ref([]);
const courses = ref([]);
const teachers = ref([]);
const majors = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showModal = ref(false);
const isEditing = ref(false);

const getNewEditableSchedule = () => ({
  id: null, courseId: '', teacherId: '', majorId: null,
  academicYear: '', semester: '', courseDay: null, courseTime: null,
});

const editableSchedule = ref(getNewEditableSchedule());

const fetchAllData = async () => {
  isLoading.value = true;
  try {
    await Promise.all([
      (async () => schedules.value = await courseService.getAllSchedules())(),
      (async () => courses.value = await courseService.getAll())(),
      (async () => teachers.value = await teacherService.getAll())(),
      (async () => majors.value = await majorService.getAll())(),
    ]);
  } catch (error) {
    showNotification(error.message || '数据加载失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchAllData);

const openAddModal = (initialData) => {
  isEditing.value = false;
  editableSchedule.value = { ...getNewEditableSchedule(), ...initialData };
  showModal.value = true;
};

const openEditModal = (schedule) => {
  isEditing.value = true;
  editableSchedule.value = { ...schedule };
  showModal.value = true;
};

const handleCellClick = ({ day, time }) => {
  openAddModal({ courseDay: day, courseTime: time });
};

const handleCourseClick = (course) => {
  openEditModal(course);
};

const closeModal = () => {
  showModal.value = false;
};

const handleSubmit = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    if (isEditing.value) {
      await courseService.updateSchedule(editableSchedule.value.id, editableSchedule.value);
      showNotification('课程安排更新成功！', 'success');
    } else {
      await courseService.createSchedule(editableSchedule.value);
      showNotification('课程安排创建成功！', 'success');
    }
    closeModal();
    await fetchAllData();
  } catch (error) {
    showNotification(error.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleDelete = async () => {
  if (!isEditing.value || !editableSchedule.value.id) return;
  if (!confirm('确定要删除这个课程安排吗？')) return;

  isSubmitting.value = true;
  try {
    await courseService.deleteSchedule(editableSchedule.value.id);
    showNotification('课程安排删除成功！', 'success');
    closeModal();
    await fetchAllData();
  } catch (error) {
    showNotification(error.message || '删除失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';
@import '@/assets/styles/common-modal.css';

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  background-color: #fff;
}
.modal-actions .delete-btn {
  background-color: #fdf2f2;
  color: var(--color-danger);
  border-color: #f5c6cb;
  margin-right: auto;
}
</style>