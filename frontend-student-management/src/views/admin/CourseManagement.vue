<template>
  <div class="page-container">
    <header class="page-header">
      <h1>课程管理</h1>
      <button @click="openAddModal" class="add-btn">新增课程</button>
    </header>

    <div class="content-card">
      <table class="data-table">
        <thead>
        <tr>
          <th>课程编号</th>
          <th>课程名称</th>
          <th>类型</th>
          <th>所属专业</th>
          <th>学分</th>
          <th>授课教师</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="isLoading">
          <td colspan="7" style="text-align: center; padding: 20px;">正在加载数据...</td>
        </tr>
        <tr v-for="course in courses" :key="course.id">
          <td>{{ course.courseId }}</td>
          <td>{{ course.courseName }}</td>
          <td>{{ course.courseType === 'COMPULSORY' ? '必修' : '选修' }}</td>
          <td>{{ course.majorName || '通用' }}</td>
          <td>{{ course.credits }}</td>
          <td>{{ course.teacherName }}</td>
          <td>
            <button @click="openEditModal(course)" class="action-btn edit">编辑</button>
            <button @click="handleDeleteCourse(course.id)" class="action-btn delete">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content" style="width: 500px;">
        <h2>{{ isEditing ? '编辑课程' : '新增课程' }}</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-grid">
            <div class="form-group">
              <label for="courseId">课程编号</label>
              <input v-model="editableCourse.courseId" id="courseId" required>
            </div>
            <div class="form-group">
              <label for="courseName">课程名称</label>
              <input v-model="editableCourse.courseName" id="courseName" required>
            </div>
            <div class="form-group">
              <label for="credits">学分</label>
              <input type="number" step="0.5" v-model="editableCourse.credits" id="credits" required>
            </div>
            <div class="form-group">
              <label for="teacherId">授课教师</label>
              <select v-model="editableCourse.teacherId" id="teacherId" required>
                <option disabled value="">请选择</option>
                <option v-for="teacher in teachers" :key="teacher.teacherId" :value="teacher.teacherId">
                  {{ teacher.name }} ({{ teacher.teacherId }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="courseType">课程类型</label>
              <select v-model="editableCourse.courseType" id="courseType" required>
                <option value="ELECTIVE">选修</option>
                <option value="COMPULSORY">必修</option>
              </select>
            </div>
            <div class="form-group">
              <label for="majorId">所属专业</label>
              <select v-model="editableCourse.majorId" id="majorId" :disabled="editableCourse.courseType !== 'COMPULSORY'">
                <option :value="null">通用（仅选修课可选）</option>
                <option v-for="major in majors" :key="major.id" :value="major.id">{{ major.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label for="academicYear">开设学年</label>
              <select v-model="editableCourse.academicYear" id="academicYear">
                <option :value="null">不限</option>
                <option v-for="n in 4" :key="n" :value="n">第 {{ n }} 学年</option>
              </select>
            </div>
            <div class="form-group">
              <label for="semester">开设学期</label>
              <select v-model="editableCourse.semester" id="semester">
                <option :value="null">不限</option>
                <option value="1">上学期</option>
                <option value="2">下学期</option>
              </select>
            </div>
            <div class="form-group">
              <label for="courseDay">上课日</label>
              <select v-model="editableCourse.courseDay" id="courseDay">
                <option :value="null">未安排</option>
                <option v-for="day in 7" :key="day" :value="day">星期{{ '一二三四五六日'[day-1] }}</option>
              </select>
            </div>
            <div class="form-group">
              <label for="courseTime">上课时段</label>
              <select v-model="editableCourse.courseTime" id="courseTime">
                <option :value="null">未安排</option>
                <option v-for="time in 5" :key="time" :value="time">第 {{ time }} 大节</option>
              </select>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" @click="closeModal">取消</button>
            <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '处理中...' : '提交' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { courseService, teacherService, majorService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const courses = ref([]);
const teachers = ref([]);
const majors = ref([]); // 新增
const isLoading = ref(true);
const isSubmitting = ref(false);
const showModal = ref(false);
const isEditing = ref(false);

const getNewEditableCourse = () => ({
  id: null, courseId: '', courseName: '', credits: null, teacherId: '',
  courseDay: null, courseTime: null,
  courseType: 'ELECTIVE', majorId: null, academicYear: null, semester: null,
});

const editableCourse = ref(getNewEditableCourse());

watch(() => editableCourse.value.courseType, (newType) => {
  if (newType === 'ELECTIVE') {
    editableCourse.value.majorId = null;
  }
});

const fetchAllData = async () => {
  isLoading.value = true;
  try {
    await Promise.all([
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

const resetForm = () => editableCourse.value = getNewEditableCourse();

const openAddModal = () => {
  resetForm();
  isEditing.value = false;
  showModal.value = true;
};

const openEditModal = (course) => {
  editableCourse.value = { ...course };
  isEditing.value = true;
  showModal.value = true;
};

const closeModal = () => showModal.value = false;

const handleSubmit = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  const payload = { ...editableCourse.value };
  if (payload.courseType === 'ELECTIVE') {
    payload.majorId = null; // 确保选修课的majorId为null
  } else if (!payload.majorId) {
    showNotification('必修课必须选择一个专业', 'error');
    isSubmitting.value = false;
    return;
  }

  try {
    if (isEditing.value) {
      await courseService.update(payload.id, payload);
      showNotification('课程更新成功！', 'success');
    } else {
      await courseService.create(payload);
      showNotification('课程创建成功！', 'success');
    }
    closeModal();
    await fetchAllData();
  } catch (error) {
    showNotification(error.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleDeleteCourse = async (courseId) => {
  if (!confirm('确定要删除这门课程吗？')) return;
  try {
    await courseService.delete(courseId);
    showNotification('课程删除成功！', 'success');
    await fetchAllData();
  } catch (error) {
    showNotification(error.message || '删除失败', 'error');
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
</style>