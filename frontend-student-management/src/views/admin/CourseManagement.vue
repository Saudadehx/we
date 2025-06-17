<template>
  <div class="page-container">
    <header class="page-header">
      <h1>课程安排管理</h1>
      <div>
        <button @click="openCatalogModal" class="action-btn-secondary">管理课程目录</button>
        <button @click="openAddModal({})" class="add-btn">新增课程安排</button>
      </div>
    </header>

    <div class="content-card">
      <p v-if="isLoading">正在加载课表数据...</p>
      <ScheduleGrid
          v-else
          :offerings="offerings"
          :majors="majors"
          @cell-click="handleCellClick"
          @offering-click="handleOfferingClick"
      />
    </div>

    <div v-if="showOfferingModal" class="modal-overlay" @click.self="closeOfferingModal">
      <div class="modal-content" style="width: 600px;">
        <h2>{{ isEditing ? '编辑课程安排' : '新增课程安排' }}</h2>
        <form @submit.prevent="handleOfferingSubmit">
          <div class="form-grid">
            <div class="form-group">
              <label for="courseCatalogId">基础课程</label>
              <select v-model="editableOffering.courseCatalogId" id="courseCatalogId" required>
                <option disabled value="">请选择</option>
                <option v-for="catalog in catalogs" :key="catalog.id" :value="catalog.id">
                  {{ catalog.name }} ({{ catalog.courseCode }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="teacherId">授课教师</label>
              <select v-model="editableOffering.teacherId" id="teacherId" required>
                <option disabled value="">请选择</option>
                <option v-for="teacher in teachers" :key="teacher.id" :value="teacher.id">
                  {{ teacher.name }} ({{ teacher.teacherId }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="academicYear">开设学年</label>
              <select v-model="editableOffering.academicYear" id="academicYear" required>
                <option disabled value="">请选择</option>
                <option v-for="n in 4" :key="n" :value="n">第 {{ n }} 学年</option>
              </select>
            </div>
            <div class="form-group">
              <label for="semester">开设学期</label>
              <select v-model="editableOffering.semester" id="semester" required>
                <option disabled value="">请选择</option>
                <option value="1">上学期</option>
                <option value="2">下学期</option>
              </select>
            </div>
            <div class="form-group">
              <label for="courseDay">上课日</label>
              <select v-model="editableOffering.courseDay" id="courseDay">
                <option :value="null">未安排</option>
                <option v-for="day in 7" :key="day" :value="day">星期{{ '一二三四五六日'[day-1] }}</option>
              </select>
            </div>
            <div class="form-group">
              <label for="courseTime">上课时段</label>
              <select v-model="editableOffering.courseTime" id="courseTime">
                <option :value="null">未安排</option>
                <option v-for="time in 5" :key="time" :value="time">第 {{ time }} 大节</option>
              </select>
            </div>
          </div>

          <div class="form-group" style="margin-top: 20px;">
            <label>关联专业 (用于指定必修/选修)</label>
            <div v-for="(link, index) in editableOffering.associatedMajors" :key="index" class="major-link-item">
              <select v-model="link.majorId">
                <option disabled value="">选择专业</option>
                <option v-for="major in majors" :key="major.id" :value="major.id">{{ major.name }}</option>
              </select>
              <select v-model="link.courseType">
                <option value="COMPULSORY">必修</option>
                <option value="ELECTIVE">选修</option>
              </select>
              <button type="button" @click="removeMajorLink(index)" class="remove-link-btn">-</button>
            </div>
            <button type="button" @click="addMajorLink" class="add-link-btn">+ 添加专业关联</button>
          </div>

          <div class="modal-actions">
            <button type="button" @click="handleOfferingDelete" class="action-btn delete" v-if="isEditing">删除</button>
            <div style="flex-grow: 1;"></div>
            <button type="button" @click="closeOfferingModal">取消</button>
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
import ScheduleGrid from '@/components/ScheduleGrid.vue'; // 确保你已创建此组件

const offerings = ref([]);
const catalogs = ref([]);
const teachers = ref([]);
const majors = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showOfferingModal = ref(false);
const isEditing = ref(false);

const getNewEditableOffering = () => ({
  id: null,
  courseCatalogId: '',
  teacherId: '',
  academicYear: '',
  semester: '',
  courseDay: null,
  courseTime: null,
  associatedMajors: []
});

const editableOffering = ref(getNewEditableOffering());

const fetchAllData = async () => {
  isLoading.value = true;
  try {
    const [offeringsRes, catalogsRes, teachersRes, majorsRes] = await Promise.all([
      courseService.getAllOfferings(),
      courseService.getAllCatalogs(),
      teacherService.getAll(),
      majorService.getAll()
    ]);
    offerings.value = offeringsRes;
    catalogs.value = catalogsRes;
    teachers.value = teachersRes;
    majors.value = majorsRes;
  } catch (error) {
    showNotification(error.message || '数据加载失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchAllData);

const openAddModal = (initialData) => {
  isEditing.value = false;
  editableOffering.value = { ...getNewEditableOffering(), ...initialData };
  showOfferingModal.value = true;
};

const openEditModal = (offering) => {
  isEditing.value = true;
  // 后端返回的数据可能没有 associatedMajors 字段，确保它是个数组
  editableOffering.value = { ...offering, associatedMajors: offering.associatedMajors || [] };
  showOfferingModal.value = true;
};

const handleCellClick = ({ day, time }) => openAddModal({ courseDay: day, courseTime: time });
const handleOfferingClick = (offering) => openEditModal(offering);
const closeOfferingModal = () => showOfferingModal.value = false;

const addMajorLink = () => {
  editableOffering.value.associatedMajors.push({ majorId: '', courseType: 'ELECTIVE' });
};
const removeMajorLink = (index) => {
  editableOffering.value.associatedMajors.splice(index, 1);
};

const handleOfferingSubmit = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;
  try {
    if (isEditing.value) {
      await courseService.updateOffering(editableOffering.value.id, editableOffering.value);
      showNotification('课程安排更新成功！', 'success');
    } else {
      await courseService.createOffering(editableOffering.value);
      showNotification('课程安排创建成功！', 'success');
    }
    closeOfferingModal();
    await fetchAllData();
  } catch (error) {
    showNotification(error.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleOfferingDelete = async () => {
  if (!isEditing.value || !editableOffering.value.id) return;
  if (!confirm('确定要删除这个课程安排吗？所有相关的选课记录也将被删除。')) return;
  isSubmitting.value = true;
  try {
    await courseService.deleteOffering(editableOffering.value.id);
    showNotification('课程安排删除成功！', 'success');
    closeOfferingModal();
    await fetchAllData();
  } catch (error) {
    showNotification(error.message || '删除失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

// 省略了管理课程目录(Catalog)的模态框逻辑，可按需添加
const openCatalogModal = () => {
  alert("管理课程目录的功能待实现。您可以在此添加一个新模态框来增删改查 `course_catalogs`。");
}
</script>

<style scoped>
@import '@/assets/styles/common-page.css';
@import '@/assets/styles/common-modal.css';

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.major-link-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.major-link-item select { flex-grow: 1; }
.remove-link-btn, .add-link-btn {
  padding: 8px;
  border: none;
  cursor: pointer;
}
.remove-link-btn { background-color: #f8d7da; color: #721c24; }
.add-link-btn { background-color: #e2e6ea; color: #495057; width: 100%; margin-top: 8px;}
.action-btn-secondary {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 1em;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: opacity 0.2s;
  margin-right: 16px;
}
</style>