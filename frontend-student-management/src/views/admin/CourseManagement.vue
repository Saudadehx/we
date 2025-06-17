<template>
  <div class="page-container">
    <div class="course-mgmt-layout">

      <div class="left-panel">
        <div class="panel-header">
          <h1>课程管理</h1>
          <button @click="openAddOfferingModal({})" class="add-btn">新增安排</button>
        </div>

        <div class="panel-content">
          <div class="filter-card">
            <div class="filter-item">
              <label>搜索课程名 / 代码 / 教师</label>
              <input type="text" v-model="filters.searchQuery" placeholder="输入关键词...">
            </div>
            <div class="filter-item">
              <label>按专业筛选</label>
              <select v-model="filters.majorId">
                <option :value="null">所有专业</option>
                <option v-for="major in majors" :key="major.id" :value="major.id">{{ major.name }}</option>
              </select>
            </div>
          </div>

          <div class="list-card">
            <div v-if="isOfferingsLoading" class="loading-placeholder"><span>加载中...</span></div>
            <ul v-else-if="filteredOfferings.length > 0" class="offering-list">
              <li
                  v-for="offering in filteredOfferings"
                  :key="offering.id"
                  @click="openEditOfferingModal(offering)"
                  @mouseenter="hoveredOfferingId = offering.id"
                  @mouseleave="hoveredOfferingId = null"
                  :class="{ 'is-active': hoveredOfferingId === offering.id }"
              >
                <strong>{{ offering.courseName }}</strong>
                <span class="course-meta">{{ offering.teacherName }} | {{ formatCourseTime(offering.courseDay, offering.courseTime) }}</span>
              </li>
            </ul>
            <div v-else class="loading-placeholder">
              <span>{{ hasFiltersApplied ? '无匹配课程' : '请先使用筛选功能' }}</span>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <button @click="openCatalogManagementModal" class="footer-btn">管理课程基础目录</button>
        </div>
      </div>

      <div class="right-panel">
        <div v-if="isOfferingsLoading" class="loading-placeholder full-height"><span>正在加载课程表...</span></div>
        <ScheduleGrid
            v-else-if="hasFiltersApplied"
            :offerings="filteredOfferings"
            :majors="majors"
            :hovered-offering-id="hoveredOfferingId"
            @cell-click="data => openAddOfferingModal(data)"
            @offering-click="openEditOfferingModal"
            @offering-hover="id => hoveredOfferingId = id"
        />
        <div v-else class="loading-placeholder full-height">
          <div class="placeholder-content">
            <svg class="placeholder-icon" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            <p>请使用左侧的筛选功能<br>来可视化课程安排</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showOfferingModal" class="modal-overlay" @click.self="closeOfferingModal">
      <div class="modal-content stylish-modal">
        <h2>{{ isEditing ? '编辑课程安排' : '新增课程安排' }}</h2>
        <form @submit.prevent="handleOfferingSubmit">
          <div class="form-grid">
            <div class="form-group"><label>基础课程</label><select v-model="editableOffering.courseCatalogId" required><option disabled value="">请选择</option><option v-for="c in catalogs" :key="c.id" :value="c.id">{{c.name}} ({{c.courseCode}})</option></select></div>
            <div class="form-group"><label>授课教师</label><select v-model="editableOffering.teacherId" required><option disabled value="">请选择</option><option v-for="t in teachers" :key="t.id" :value="t.id">{{t.name}} ({{t.teacherId}})</option></select></div>
            <div class="form-group"><label>开设学年</label><select v-model.number="editableOffering.academicYear" required><option v-for="n in 4" :key="n" :value="n">第 {{n}} 学年</option></select></div>
            <div class="form-group"><label>开设学期</label><select v-model.number="editableOffering.semester" required><option value="1">上学期</option><option value="2">下学期</option></select></div>
            <div class="form-group"><label>上课日</label><select v-model.number="editableOffering.courseDay"><option :value="null">未安排</option><option v-for="d in 7" :key="d" :value="d">星期{{'一二三四五六日'[d-1]}}</option></select></div>
            <div class="form-group"><label>上课时段</label><select v-model.number="editableOffering.courseTime"><option :value="null">未安排</option><option v-for="t in 5" :key="t" :value="t">第 {{t}} 大节</option></select></div>
          </div>
          <div class="form-group" style="margin-top: 20px;"><label>关联专业 (用于指定必修/选修)</label>
            <div v-for="(link, index) in editableOffering.associatedMajors" :key="index" class="major-link-item">
              <select v-model="link.majorId"><option disabled value="">选择专业</option><option v-for="m in majors" :key="m.id" :value="m.id">{{m.name}}</option></select>
              <select v-model="link.courseType"><option value="COMPULSORY">必修</option><option value="ELECTIVE">选修</option></select>
              <button type="button" @click="removeMajorLink(index)" class="remove-link-btn">-</button>
            </div>
            <button type="button" @click="addMajorLink" class="add-link-btn">+ 添加专业关联</button>
          </div>
          <div class="modal-actions"><button type="button" @click="handleOfferingDelete" class="action-btn delete" v-if="isEditing">删除</button><div style="flex-grow: 1;"></div><button type="button" @click="closeOfferingModal">取消</button><button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '处理中...' : '提交' }}</button></div>
        </form>
      </div>
    </div>
    <div v-if="showCatalogModal" class="modal-overlay" @click.self="closeCatalogManagementModal">
      <div class="modal-content stylish-modal" style="width: 800px; max-width: 90vw;">
        <div v-if="isEditingCatalog">
          <h2>{{ editableCatalog.id ? '编辑课程' : '新增课程' }}</h2>
          <form @submit.prevent="handleCatalogSubmit">
            <div class="form-grid">
              <div class="form-group"><label>课程名称</label><input v-model="editableCatalog.name" required></div>
              <div class="form-group"><label>课程代码</label><input v-model="editableCatalog.courseCode" required></div>
              <div class="form-group"><label>学分</label><input v-model.number="editableCatalog.credits" type="number" step="0.5" min="0.5" required></div>
            </div>
            <div class="modal-actions"><button type="button" @click="isEditingCatalog = false">返回列表</button><button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '提交中...' : '提交' }}</button></div>
          </form>
        </div>
        <div v-else>
          <header class="modal-page-header"><h2>课程基础目录管理</h2><button @click="handleAddNewCatalog" class="add-btn">新增</button></header>
          <div class="table-container">
            <table class="data-table">
              <thead><tr><th>ID</th><th>代码</th><th>名称</th><th>学分</th><th>操作</th></tr></thead>
              <tbody>
              <tr v-if="isCatalogsLoading"><td colspan="5" class="loading-placeholder">加载目录...</td></tr>
              <tr v-else v-for="catalog in catalogs" :key="catalog.id">
                <td>{{ catalog.id }}</td><td>{{ catalog.courseCode }}</td><td>{{ catalog.name }}</td><td>{{ catalog.credits }}</td>
                <td><button @click="handleEditCatalog(catalog)" class="action-btn edit">编辑</button><button @click="handleCatalogDelete(catalog.id)" class="action-btn delete">删除</button></td>
              </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-actions"><button type="button" @click="closeCatalogManagementModal">关闭</button></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { courseService, teacherService, majorService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';
import ScheduleGrid from '@/components/ScheduleGrid.vue';

// --- 状态定义 ---
const allOfferings = ref([]);
const teachers = ref([]);
const majors = ref([]);
const catalogs = ref([]);
const isOfferingsLoading = ref(true);
const isCatalogsLoading = ref(false);
const isSubmitting = ref(false);
const showOfferingModal = ref(false);
const showCatalogModal = ref(false);
const isEditing = ref(false);
const isEditingCatalog = ref(false);

const getNewEditableOffering = () => ({ id: null, courseCatalogId: '', teacherId: '', academicYear: 1, semester: 1, courseDay: null, courseTime: null, associatedMajors: [] });
const editableOffering = ref(getNewEditableOffering());
const editableCatalog = ref(null);
const hoveredOfferingId = ref(null);

const filters = ref({ majorId: null, searchQuery: '' });

// --- 计算属性 ---
const hasFiltersApplied = computed(() => filters.value.majorId !== null || filters.value.searchQuery !== '');

const filteredOfferings = computed(() => {
  // 如果没有应用任何筛选，返回空数组，避免在右侧课程表显示全部课程
  if (!hasFiltersApplied.value) return [];

  return allOfferings.value.filter(offering => {
    const { majorId, searchQuery } = filters.value;
    const searchLower = searchQuery.toLowerCase();

    const majorMatch = !majorId || (offering.associatedMajors && offering.associatedMajors.some(m => m.majorId === majorId));
    const searchMatch = !searchQuery ||
        (offering.courseName?.toLowerCase().includes(searchLower)) ||
        (offering.courseCode?.toLowerCase().includes(searchLower)) ||
        (offering.teacherName?.toLowerCase().includes(searchLower));

    return majorMatch && searchMatch;
  });
});

// --- 数据获取 ---
const fetchAllData = async () => {
  isOfferingsLoading.value = true;
  try {
    const [offeringsRes, teachersRes, majorsRes, catalogsRes] = await Promise.all([
      courseService.getAllOfferings(),
      teacherService.getAll(),
      majorService.getAll(),
      courseService.getAllCatalogs(),
    ]);
    allOfferings.value = offeringsRes;
    teachers.value = teachersRes;
    majors.value = majorsRes;
    catalogs.value = catalogsRes;
  } catch (e) { showNotification(e.message || '数据加载失败', 'error'); } finally { isOfferingsLoading.value = false; }
};
onMounted(fetchAllData);

// --- 方法 ---
const formatCourseTime = (d, t) => !d || !t ? '时间待定' : `周${'一二三四五六日'[d-1]}, 第${t}节`;

const openAddOfferingModal = (initialData = {}) => {
  isEditing.value = false;
  editableOffering.value = { ...getNewEditableOffering(), ...initialData };
  showOfferingModal.value = true;
};
const openEditOfferingModal = (offering) => {
  isEditing.value = true;
  editableOffering.value = structuredClone({ ...offering, associatedMajors: offering.associatedMajors || [] });
  showOfferingModal.value = true;
};
const closeOfferingModal = () => { showOfferingModal.value = false; };
const addMajorLink = () => editableOffering.value.associatedMajors.push({ majorId: '', courseType: 'ELECTIVE' });
const removeMajorLink = (index) => editableOffering.value.associatedMajors.splice(index, 1);

const handleOfferingSubmit = async () => {
  isSubmitting.value = true;
  try {
    const action = isEditing.value ? courseService.updateOffering(editableOffering.value.id, editableOffering.value) : courseService.createOffering(editableOffering.value);
    await action;
    showNotification(`课程安排${isEditing.value ? '更新' : '创建'}成功！`, 'success');
    closeOfferingModal();
    await fetchAllData();
  } catch (e) { showNotification(e.message || '操作失败', 'error'); } finally { isSubmitting.value = false; }
};

const handleOfferingDelete = async () => {
  if (!isEditing.value || !confirm('确定要删除这个课程安排吗？')) return;
  isSubmitting.value = true;
  try {
    await courseService.deleteOffering(editableOffering.value.id);
    showNotification('删除成功！', 'success');
    closeOfferingModal();
    await fetchAllData();
  } catch (e) { showNotification(e.message || '删除失败', 'error'); } finally { isSubmitting.value = false; }
};

const openCatalogManagementModal = async () => {
  isCatalogsLoading.value = true;
  isEditingCatalog.value = false;
  showCatalogModal.value = true;
  try {
    catalogs.value = await courseService.getAllCatalogs();
  } catch (e) { showNotification(e.message || '获取目录失败', 'error'); } finally { isCatalogsLoading.value = false; }
};
const closeCatalogManagementModal = () => { showCatalogModal.value = false; };
const handleAddNewCatalog = () => {
  editableCatalog.value = { id: null, name: '', courseCode: '', credits: 1.0 };
  isEditingCatalog.value = true;
};
const handleEditCatalog = (c) => {
  editableCatalog.value = { ...c };
  isEditingCatalog.value = true;
};

const handleCatalogSubmit = async () => {
  isSubmitting.value = true;
  try {
    const action = editableCatalog.value.id ? courseService.updateCatalog(editableCatalog.value.id, editableCatalog.value) : courseService.createCatalog(editableCatalog.value);
    await action;
    showNotification('操作成功！', 'success');
    isEditingCatalog.value = false;
    await openCatalogManagementModal();
  } catch (e) { showNotification(e.message || '操作失败', 'error'); } finally { isSubmitting.value = false; }
};

const handleCatalogDelete = async (id) => {
  if (!confirm('确定要删除吗？')) return;
  try {
    await courseService.deleteCatalog(id);
    showNotification('删除成功！', 'success');
    await openCatalogManagementModal();
  } catch (e) { showNotification(e.message || '删除失败', 'error'); }
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';
@import '@/assets/styles/common-modal.css';

/* --- 整体布局 --- */
.page-container { padding: 16px; background-color: #f4f6f9; font-family: var(--font-family-base); }
.course-mgmt-layout { display: flex; height: calc(100vh - 32px); gap: 16px; }

/* --- 左右面板通用样式 --- */
.left-panel, .right-panel {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e9ecef;
}
.left-panel { width: 360px; flex-shrink: 0; }
.right-panel { flex-grow: 1; padding: 16px; }

/* --- 面板内部结构 --- */
.panel-header { padding: 16px 20px; border-bottom: 1px solid #e9ecef; display: flex; justify-content: space-between; align-items: center; }
.panel-header h1 { font-size: 1.25em; margin: 0; font-weight: 600; }
.panel-content { padding: 16px; overflow-y: auto; flex-grow: 1; background-color: #fbfcfd; }
.panel-footer { padding: 16px; border-top: 1px solid #e9ecef; }

/* --- 筛选与列表 --- */
.filter-card { padding: 16px; background-color: #fff; border-radius: 8px; margin-bottom: 16px; }
.filter-item { margin-bottom: 12px; }
.filter-item:last-child { margin-bottom: 0; }
.filter-item label { font-size: 0.8em; font-weight: 600; margin-bottom: 8px; color: #6c757d; display: block; text-transform: uppercase; letter-spacing: 0.5px; }
.filter-item select, .filter-item input { width: 100%; box-sizing: border-box; padding: 10px 12px; border: 1px solid #ced4da; border-radius: 6px; font-size: 0.9em; transition: all 0.2s; }
.filter-item select:focus, .filter-item input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-light); outline: none; }

.list-card { border-radius: 8px; overflow: hidden; height: 100%; display: flex; flex-direction: column; }
.offering-list { list-style: none; padding: 0; margin: 0; flex-grow: 1; overflow-y: auto; }
.offering-list li { padding: 12px 16px; cursor: pointer; border-bottom: 1px solid #f1f3f5; transition: background-color 0.2s, transform 0.2s; }
.offering-list li:last-child { border-bottom: none; }
.offering-list li.is-active, .offering-list li:hover { background-color: var(--color-primary-light); transform: translateX(4px); }
.offering-list li strong { display: block; font-weight: 600; color: var(--color-text-primary); }
.offering-list li .course-meta { font-size: 0.85em; color: var(--color-text-secondary); }

/* --- 占位符与页脚按钮 --- */
.loading-placeholder { display: flex; justify-content: center; align-items: center; height: 100%; min-height: 150px; color: #adb5bd; font-size: 0.9em; text-align: center; }
.loading-placeholder.full-height { min-height: 100%; }
.placeholder-content { display: flex; flex-direction: column; align-items: center; gap: 16px; }
.placeholder-icon { color: #dee2e6; }
.footer-btn {
  width: 100%;
  padding: 14px; /* 增大按钮 */
  font-size: 1em; /* 增大字体 */
  font-weight: 500;
  background-color: #f8f9fa;
  color: var(--color-text-primary);
  border: 1px solid #dee2e6;
  border-radius: 8px; /* 统一圆角 */
  transition: all 0.2s;
}
.footer-btn:hover { background-color: #e9ecef; border-color: #ced4da; }

/* --- 美化后的模态框 --- */
.stylish-modal { width: 650px; max-width: 95vw; }
.stylish-modal h2 { margin-bottom: 24px; font-size: 1.5em; font-weight: 600; }
.stylish-modal .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px 24px; }
.stylish-modal .form-group { margin-bottom: 0; }
.stylish-modal .form-group label { margin-bottom: 8px; font-weight: 500; font-size: 0.9em; }
.stylish-modal .form-group input, .stylish-modal .form-group select { padding-top: 10px; padding-bottom: 10px; }
.stylish-modal .major-link-item, .stylish-modal .add-link-btn { margin-top: 8px; }

/* --- 课程目录模态框内表格 --- */
.table-container { max-height: 60vh; overflow-y: auto; border: 1px solid #dee2e6; border-radius: var(--border-radius); }
.modal-page-header { margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; }
.modal-page-header h2 { margin: 0; font-size: 1.5em; font-weight: 600; }
</style>