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
            <div class="filter-item">
              <label>按班级筛选</label>
              <select v-model="filters.classId" :disabled="!filters.majorId">
                <option :value="null">所有班级</option>
                <option v-for="cls in classesFilteredByMajor" :key="cls.id" :value="cls.id">{{ cls.name }}</option>
              </select>
            </div>
          </div>

          <div class="list-card">
            <div v-if="isOfferingsLoading" class="loading-placeholder"><span>加载中...</span></div>
            <ul v-else-if="filteredOfferings.length > 0" class="offering-list">
              <li
                  v-for="offering in paginatedOfferings"
                  :key="offering.id"
                  class="offering-list-item"
                  @mouseenter="hoveredOfferingId = offering.id"
                  @mouseleave="hoveredOfferingId = null"
                  :class="{ 'is-active': hoveredOfferingId === offering.id }"
              >
                <div class="item-info">
                  <strong>{{ offering.courseName }}</strong>
                  <span class="course-meta">{{ offering.teacherName }} | {{ formatCourseTime(offering.courseDay, offering.courseTime) }}</span>
                </div>
                <div class="item-actions">
                  <button class="action-btn-sm edit" @click="openEditOfferingModal(offering)">编辑</button>
                  <button class="action-btn-sm delete" @click="handleDeleteOffering(offering.id)">删除</button>
                </div>
              </li>
            </ul>
            <div v-else class="loading-placeholder">
              <span>{{ hasFiltersApplied ? '无匹配课程' : '请先筛选课程' }}</span>
            </div>

            <div v-if="totalPages > 1" class="pagination-controls">
              <button
                  :disabled="currentPage === 1"
                  @click="currentPage = Math.max(1, currentPage - 1)"
                  class="pagination-btn"
              >
                上一页
              </button>
              <span class="pagination-info">{{ currentPage }} / {{ totalPages }}</span>
              <button
                  :disabled="currentPage === totalPages"
                  @click="currentPage = Math.min(totalPages, currentPage + 1)"
                  class="pagination-btn"
              >
                下一页
              </button>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <button @click="openCatalogManagementModal" class="footer-btn">管理课程基础目录</button>
        </div>
      </div>

      <div class="right-panel">
        <div v-if="isOfferingsLoading" class="loading-placeholder full-height"><span>正在加载课程表...</span></div>
        <div class="schedule-container" v-else-if="hasFiltersApplied">
          <ScheduleGrid
              :offerings="filteredOfferings"
              :classes="classes"
              :hovered-offering-id="hoveredOfferingId"
              @cell-click="data => openAddOfferingModal(data)"
              @offering-click="openEditOfferingModal"
              @offering-hover="id => hoveredOfferingId = id"
          />
        </div>
        <div v-else class="loading-placeholder full-height">
          <div class="placeholder-content">
            <svg class="placeholder-icon" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            <p>请使用左侧的筛选功能<br>来可视化课程安排</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showOfferingModal" class="modal-overlay" @click.self="closeOfferingModal">
      <div class="modal-content stylish-modal redesigned-modal">
        <header class="modal-header-redesigned">
          <h2>{{ isEditingOffering ? '编辑课程安排' : '新增课程安排' }}</h2>
          <p>请填写课程的具体教学安排和面向对象。</p>
        </header>

        <form @submit.prevent="handleOfferingSubmit">
          <div class="modal-body-redesigned">
            <fieldset class="form-section">
              <legend>核心安排</legend>
              <div class="form-row">
                <div class="form-group flex-grow">
                  <label>基础课程</label>
                  <select v-model="editableOffering.courseCatalogId" required>
                    <option disabled value="">请选择一个基础课程</option>
                    <option v-for="c in catalogs" :key="c.id" :value="c.id">{{c.name}} ({{c.courseCode}})</option>
                  </select>
                </div>
                <div class="form-group flex-grow">
                  <label>授课教师</label>
                  <select v-model="editableOffering.teacherId" required>
                    <option disabled value="">请选择一位授课教师</option>
                    <option v-for="t in teachers" :key="t.id" :value="t.id">{{t.name}} ({{t.teacherId}})</option>
                  </select>
                </div>
              </div>
            </fieldset>

            <fieldset class="form-section">
              <legend>时间计划</legend>
              <div class="form-row">
                <div class="form-group">
                  <label>开设学年</label>
                  <select v-model.number="editableOffering.academicYear" required>
                    <option v-for="n in 4" :key="n" :value="n">第 {{n}} 学年</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>开设学期</label>
                  <select v-model.number="editableOffering.semester" required>
                    <option value="1">上学期</option>
                    <option value="2">下学期</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>上课日</label>
                  <select v-model.number="editableOffering.courseDay">
                    <option :value="null">未安排</option>
                    <option v-for="d in 7" :key="d" :value="d">星期{{'一二三四五六日'[d-1]}}</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>上课时段</label>
                  <select v-model.number="editableOffering.courseTime">
                    <option :value="null">未安排</option>
                    <option v-for="t in 5" :key="t" :value="t">第 {{t}} 大节</option>
                  </select>
                </div>
              </div>
            </fieldset>

            <fieldset class="form-section">
              <legend>教学对象 (用于指定必修/选修)</legend>
              <div class="major-links-container">
                <div v-for="(link, index) in editableOffering.associatedClasses" :key="index" class="major-link-item-redesigned">
                  <div class="form-group flex-grow">
                    <select v-model="link.classId" class="major-select">
                      <option disabled value="">选择班级</option>
                      <optgroup v-for="major in majors" :key="major.id" :label="major.name">
                        <option v-for="cls in getClassesByMajorId(major.id)" :key="cls.id" :value="cls.id">
                          {{ cls.name }}
                        </option>
                      </optgroup>
                    </select>
                  </div>
                  <div class="form-group">
                    <select v-model="link.courseType" class="type-select">
                      <option value="COMPULSORY">必修</option>
                      <option value="ELECTIVE">选修</option>
                    </select>
                  </div>
                  <button type="button" @click="removeClassLink(index)" class="remove-link-btn-redesigned">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                  </button>
                </div>
              </div>
              <button type="button" @click="addClassLink" class="add-link-btn-redesigned">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                <span>添加班级关联</span>
              </button>
            </fieldset>
          </div>

          <div class="modal-actions">
            <button type="button" @click="handleDeleteOffering(editableOffering.id)" class="delete-btn-redesigned" v-if="isEditingOffering">删除</button>
            <div style="flex-grow: 1;"></div>
            <button type="button" @click="closeOfferingModal" class="cancel-btn-redesigned">取消</button>
            <button type="submit" :disabled="isSubmitting" class="submit-btn-redesigned">
              {{ isSubmitting ? '处理中...' : (isEditingOffering ? '保存更新' : '确认新增') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showCatalogModal" class="modal-overlay" @click.self="closeCatalogManagementModal">
      <div class="modal-content catalog-modal">
        <div v-if="isEditingCatalog" class="catalog-edit-view">
          <div class="catalog-edit-header">
            <button @click="isEditingCatalog = false" class="back-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m12 19-7-7 7-7"/><path d="m19 12H5"/>
              </svg>
              返回列表
            </button>
            <h2>{{ editableCatalog.id ? '编辑课程' : '新增课程' }}</h2>
          </div>

          <form @submit.prevent="handleCatalogSubmit" class="catalog-form">
            <div class="catalog-form-grid">
              <div class="form-group">
                <label>课程名称 <span class="required">*</span></label>
                <input v-model="editableCatalog.name" required placeholder="请输入课程名称">
              </div>
              <div class="form-group">
                <label>课程代码 <span class="required">*</span></label>
                <input v-model="editableCatalog.courseCode" required placeholder="请输入课程代码">
              </div>
              <div class="form-group">
                <label>学分 <span class="required">*</span></label>
                <input v-model.number="editableCatalog.credits" type="number" step="0.5" min="0.5" required placeholder="请输入学分">
              </div>
            </div>

            <div class="catalog-form-actions">
              <button type="button" @click="isEditingCatalog = false" class="cancel-btn">取消</button>
              <button type="submit" :disabled="isSubmitting" class="submit-btn">
                {{ isSubmitting ? '提交中...' : '提交' }}
              </button>
            </div>
          </form>
        </div>

        <div v-else class="catalog-list-view">
          <div class="catalog-header">
            <div class="catalog-header-content">
              <div class="catalog-title-section">
                <h2>课程基础目录管理</h2>
                <p class="catalog-subtitle">管理所有可开设的课程信息</p>
              </div>
              <div class="catalog-header-actions">
                <div class="catalog-search-container">
                  <input
                      type="text"
                      v-model="catalogSearchQuery"
                      placeholder="搜索课程名称或代码..."
                      class="catalog-search-input"
                  >
                </div>
                <button @click="handleAddNewCatalog" class="catalog-add-btn">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line>
                  </svg>
                  新增课程
                </button>
              </div>
            </div>
          </div>

          <div class="catalog-content">
            <div v-if="isCatalogsLoading" class="catalog-loading">
              <div class="loading-spinner"></div>
              <span>正在加载课程目录...</span>
            </div>

            <div v-else-if="filteredCatalogs.length === 0" class="catalog-empty">
              <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14,2 14,8 20,8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10,9 9,9 8,9"></polyline>
              </svg>
              <h3>{{ catalogSearchQuery ? '未找到匹配的课程' : '暂无课程目录' }}</h3>
              <p>{{ catalogSearchQuery ? '请尝试其他搜索关键词' : '点击上方"新增课程"按钮来创建第一个课程' }}</p>
            </div>

            <div v-else class="catalog-content-wrapper">
              <div class="catalog-virtual-list" ref="catalogListContainer">
                <div
                    v-for="catalog in visibleCatalogs"
                    :key="catalog.id"
                    class="catalog-row"
                >
                  <div class="catalog-row-content">
                    <div class="catalog-basic-info">
                      <div class="catalog-name-section">
                        <h4 class="catalog-name">{{ catalog.name }}</h4>
                        <span class="catalog-code">{{ catalog.courseCode }}</span>
                      </div>
                      <div class="catalog-credits-section">
                        <span class="credits-label">学分</span>
                        <span class="credits-value">{{ catalog.credits }}</span>
                      </div>
                    </div>

                    <div class="catalog-row-actions">
                      <button @click="handleEditCatalog(catalog)" class="catalog-edit-btn-sm">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                          <path d="m18.5 2.5 a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                        </svg>
                        编辑
                      </button>
                      <button @click="handleCatalogDelete(catalog.id)" class="catalog-delete-btn-sm">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                          <polyline points="3,6 5,6 21,6"></polyline>
                          <path d="m19,6v14a2,2 0 0,1-2,2H7a2,2 0 0,1-2-2V6m3,0V4a2,2 0 0,1 2-2h4a2,2 0 0,1 2,2v2"></path>
                        </svg>
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="catalogTotalPages > 1" class="catalog-pagination">
                <button
                    :disabled="catalogCurrentPage === 1"
                    @click="catalogCurrentPage = Math.max(1, catalogCurrentPage - 1)"
                    class="catalog-pagination-btn"
                >
                  上一页
                </button>
                <span class="catalog-pagination-info">
                  第 {{ catalogCurrentPage }} 页 / 共 {{ catalogTotalPages }} 页 ({{ filteredCatalogs.length }} 门课程)
                </span>
                <button
                    :disabled="catalogCurrentPage === catalogTotalPages"
                    @click="catalogCurrentPage = Math.min(catalogTotalPages, catalogCurrentPage + 1)"
                    class="catalog-pagination-btn"
                >
                  下一页
                </button>
              </div>
            </div>
          </div>

          <div class="catalog-footer">
            <button type="button" @click="closeCatalogManagementModal" class="catalog-close-btn">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { courseService, teacherService, majorService, classService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';
import ScheduleGrid from '@/components/ScheduleGrid.vue';

const allOfferings = ref([]);
const teachers = ref([]);
const majors = ref([]);
const catalogs = ref([]);
const classes = ref([]);

const isOfferingsLoading = ref(true);
const isCatalogsLoading = ref(false);
const isSubmitting = ref(false);
const hoveredOfferingId = ref(null);
const filters = ref({ majorId: null, classId: null, searchQuery: '' });

const currentPage = ref(1);
const pageSize = ref(20);
const catalogCurrentPage = ref(1);
const catalogPageSize = ref(20);
const catalogSearchQuery = ref('');

const showOfferingModal = ref(false);
const isEditingOffering = ref(false);
const getNewEditableOffering = () => ({ id: null, courseCatalogId: '', teacherId: '', academicYear: 1, semester: 1, courseDay: null, courseTime: null, associatedClasses: [] });
const editableOffering = ref(getNewEditableOffering());

const showCatalogModal = ref(false);
const isEditingCatalog = ref(false);
const editableCatalog = ref({ id: null, name: '', courseCode: '', credits: 1.0 });

const hasFiltersApplied = computed(() => filters.value.majorId !== null || filters.value.classId !== null || filters.value.searchQuery !== '');

const classesFilteredByMajor = computed(() => {
  if (!filters.value.majorId) return [];
  return classes.value.filter(cls => cls.majorId === filters.value.majorId);
});

const filteredOfferings = computed(() => {
  if (!hasFiltersApplied.value) return [];
  return allOfferings.value.filter(offering => {
    const { majorId, classId, searchQuery } = filters.value;
    const searchLower = searchQuery.toLowerCase();

    const classMatch = !classId || (offering.associatedClasses && offering.associatedClasses.some(c => c.classId === classId));

    const majorMatch = !majorId || (offering.associatedClasses && offering.associatedClasses.some(c => {
      const cls = classes.value.find(cl => cl.id === c.classId);
      return cls && cls.majorId === majorId;
    }));

    const searchMatch = !searchQuery ||
        (offering.courseName?.toLowerCase().includes(searchLower)) ||
        (offering.courseCode?.toLowerCase().includes(searchLower)) ||
        (offering.teacherName?.toLowerCase().includes(searchLower));

    return classMatch && majorMatch && searchMatch;
  });
});

const totalPages = computed(() => Math.ceil(filteredOfferings.value.length / pageSize.value));
const paginatedOfferings = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredOfferings.value.slice(start, end);
});

const filteredCatalogs = computed(() => {
  if (!catalogSearchQuery.value) return catalogs.value;
  const query = catalogSearchQuery.value.toLowerCase();
  return catalogs.value.filter(catalog =>
      catalog.name.toLowerCase().includes(query) ||
      catalog.courseCode.toLowerCase().includes(query)
  );
});

const catalogTotalPages = computed(() => Math.ceil(filteredCatalogs.value.length / catalogPageSize.value));
const visibleCatalogs = computed(() => {
  const start = (catalogCurrentPage.value - 1) * catalogPageSize.value;
  const end = start + catalogPageSize.value;
  return filteredCatalogs.value.slice(start, end);
});

const fetchAllData = async () => {
  isOfferingsLoading.value = true;
  try {
    const [offeringsRes, teachersRes, majorsRes, catalogsRes, classesRes] = await Promise.all([
      courseService.getAllOfferings(),
      teacherService.getAll(),
      majorService.getAll(),
      courseService.getAllCatalogs(),
      classService.getAll()
    ]);
    allOfferings.value = offeringsRes;
    teachers.value = teachersRes;
    majors.value = majorsRes;
    catalogs.value = catalogsRes;
    classes.value = classesRes;
  } catch (e) {
    showNotification(e.message || '数据加载失败', 'error');
  } finally {
    isOfferingsLoading.value = false;
  }
};

onMounted(fetchAllData);

watch(() => filters.value.majorId, () => {
  filters.value.classId = null;
});

watch([() => filters.value.majorId, () => filters.value.classId, () => filters.value.searchQuery], () => {
  currentPage.value = 1;
});

watch(catalogSearchQuery, () => {
  catalogCurrentPage.value = 1;
});

const openAddOfferingModal = (initialData = {}) => {
  isEditingOffering.value = false;
  editableOffering.value = { ...getNewEditableOffering(), ...initialData };
  showOfferingModal.value = true;
};

const openEditOfferingModal = (offering) => {
  isEditingOffering.value = true;
  editableOffering.value = JSON.parse(JSON.stringify(offering));
  if (!editableOffering.value.associatedClasses) {
    editableOffering.value.associatedClasses = [];
  }
  showOfferingModal.value = true;
};

const closeOfferingModal = () => {
  showOfferingModal.value = false;
};

const addClassLink = () => {
  editableOffering.value.associatedClasses.push({ classId: '', courseType: 'ELECTIVE' });
};

const removeClassLink = (index) => {
  editableOffering.value.associatedClasses.splice(index, 1);
};

const handleOfferingSubmit = async () => {
  isSubmitting.value = true;
  try {
    const action = isEditingOffering.value
        ? courseService.updateOffering(editableOffering.value.id, editableOffering.value)
        : courseService.createOffering(editableOffering.value);
    await action;
    showNotification(`课程安排${isEditingOffering.value ? '更新' : '创建'}成功！`, 'success');
    closeOfferingModal();
    await fetchAllData();
  } catch (e) {
    showNotification(e.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleDeleteOffering = async (id) => {
  if (!id || !confirm('确定要删除这个课程安排吗？相关的学生选课记录也会被删除。')) return;
  isSubmitting.value = true;
  try {
    await courseService.deleteOffering(id);
    showNotification('删除成功！', 'success');
    if (showOfferingModal.value && editableOffering.value.id === id) {
      closeOfferingModal();
    }
    await fetchAllData();
  } catch (e) {
    showNotification(e.message || '删除失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const openCatalogManagementModal = async () => {
  isEditingCatalog.value = false;
  showCatalogModal.value = true;
  catalogCurrentPage.value = 1;
  catalogSearchQuery.value = '';
  isCatalogsLoading.value = true;
  try {
    catalogs.value = await courseService.getAllCatalogs();
  } catch (e) {
    showNotification(e.message || '获取目录失败', 'error');
  } finally {
    isCatalogsLoading.value = false;
  }
};

const closeCatalogManagementModal = () => {
  showCatalogModal.value = false;
};

const handleAddNewCatalog = () => {
  editableCatalog.value = { id: null, name: '', courseCode: '', credits: 1.0 };
  isEditingCatalog.value = true;
};

const handleEditCatalog = (catalog) => {
  editableCatalog.value = { ...catalog };
  isEditingCatalog.value = true;
};

const handleCatalogSubmit = async () => {
  isSubmitting.value = true;
  try {
    const action = editableCatalog.value.id
        ? courseService.updateCatalog(editableCatalog.value.id, editableCatalog.value)
        : courseService.createCatalog(editableCatalog.value);
    await action;
    showNotification('操作成功！', 'success');
    isEditingCatalog.value = false;
    await fetchAllData();
  } catch (e) {
    showNotification(e.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleCatalogDelete = async (id) => {
  if (!confirm('确定要删除吗？如果该课程已被使用，删除将会失败。')) return;
  try {
    await courseService.deleteCatalog(id);
    showNotification('删除成功！', 'success');
    await fetchAllData();
  } catch (e) {
    showNotification(e.message || '删除失败', 'error');
  }
};

const formatCourseTime = (day, time) => {
  if (!day || !time) return '时间待定';
  return `周${'一二三四五六日'[day - 1]}, 第${time}节`;
};

const getClassesByMajorId = (majorId) => {
  return classes.value.filter(c => c.majorId === majorId);
};
</script>

<style scoped>
.page-container { padding: 16px; background-color: #f4f6f9; font-family: var(--font-family-base); }
.course-mgmt-layout { display: flex; height: calc(100vh - 32px); gap: 16px; }
.left-panel, .right-panel { background-color: #fff; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.05); display: flex; flex-direction: column; overflow: hidden; border: 1px solid #e9ecef; }
.left-panel { width: 360px; flex-shrink: 0; }
.right-panel { flex-grow: 1; padding: 16px; }

/* 优化右侧课程表容器 - 确保完整显示和响应式缩放 */
.schedule-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.panel-header { padding: 16px 20px; border-bottom: 1px solid #e9ecef; display: flex; justify-content: space-between; align-items: center; }
.panel-header h1 { font-size: 1.25em; margin: 0; font-weight: 600; }
.add-btn { background-color: var(--color-primary); color: white; border: none; padding: 8px 16px; font-size: 0.9em; border-radius: var(--border-radius); cursor: pointer; transition: opacity 0.2s; }
.panel-content { padding: 16px; overflow-y: auto; flex-grow: 1; background-color: #fbfcfd; }
.panel-footer { padding: 16px; border-top: 1px solid #e9ecef; }
.filter-card { padding: 16px; background-color: #fff; border-radius: 8px; margin-bottom: 16px; border: 1px solid #e9ecef; }
.filter-item { margin-bottom: 12px; }
.filter-item:last-child { margin-bottom: 0; }
.filter-item label { font-size: 0.8em; font-weight: 600; margin-bottom: 8px; color: #6c757d; display: block; text-transform: uppercase; letter-spacing: 0.5px; }
.filter-item select, .filter-item input { width: 100%; box-sizing: border-box; padding: 10px 12px; border: 1px solid #ced4da; border-radius: 6px; font-size: 0.9em; transition: all 0.2s; }
.filter-item select:focus, .filter-item input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-light); outline: none; }
.list-card { border-radius: 8px; overflow: hidden; height: 100%; display: flex; flex-direction: column; }
.offering-list { list-style: none; padding: 0; margin: 0; flex-grow: 1; overflow-y: auto; }
.offering-list-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #f1f3f5; transition: background-color 0.2s; }
.offering-list-item.is-active, .offering-list-item:hover { background-color: var(--color-primary-light); }
.item-info { display: flex; flex-direction: column; }
.item-info strong { font-weight: 600; color: var(--color-text-primary); }
.item-info .course-meta { font-size: 0.85em; color: var(--color-text-secondary); }
.item-actions { display: flex; gap: 8px; flex-shrink: 0; }
.action-btn-sm { padding: 4px 10px; font-size: 0.8em; border: 1px solid transparent; border-radius: 4px; cursor: pointer; font-weight: 500; transition: all 0.2s; }
.action-btn-sm.edit { background-color: #e7f5ff; color: #1c7ed6; }
.action-btn-sm.edit:hover { background-color: #d0ebff; }
.action-btn-sm.delete { background-color: #fff5f5; color: #c92a2a; }
.action-btn-sm.delete:hover { background-color: #ffe3e3; }
.loading-placeholder { display: flex; justify-content: center; align-items: center; height: 100%; min-height: 150px; color: #adb5bd; font-size: 0.9em; text-align: center; }
.loading-placeholder.full-height { min-height: 100%; }
.placeholder-content { display: flex; flex-direction: column; align-items: center; gap: 16px; }
.placeholder-icon { color: #dee2e6; }

.pagination-controls { display: flex; justify-content: center; align-items: center; gap: 12px; padding: 16px; border-top: 1px solid #e9ecef; background-color: #f8f9fa; }
.pagination-btn { padding: 6px 12px; border: 1px solid #dee2e6; background-color: #fff; color: #495057; border-radius: 4px; cursor: pointer; font-size: 0.9em; transition: all 0.2s; }
.pagination-btn:hover:not(:disabled) { background-color: #e9ecef; border-color: #adb5bd; }
.pagination-btn:disabled { background-color: #f8f9fa; color: #adb5bd; cursor: not-allowed; }
.pagination-info { font-size: 0.9em; color: #6c757d; font-weight: 500; }

.footer-btn { width: 100%; padding: 14px; font-size: 1em; font-weight: 500; background-color: #f8f9fa; color: var(--color-text-primary); border: 1px solid #dee2e6; border-radius: 8px; transition: all 0.2s; }
.footer-btn:hover { background-color: #e9ecef; border-color: #ced4da; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background-color: white; padding: 0; border-radius: 12px; width: 400px; max-width: 90%; box-shadow: 0 5px 15px rgba(0,0,0,0.3); }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
.form-group input, .form-group select { width: 100%; padding: 10px 14px; border: 1px solid var(--color-border); border-radius: var(--border-radius); box-sizing: border-box; font-family: inherit; font-size: 1em; background-color: var(--color-surface); outline: none; transition: all var(--transition-speed) ease; }
.form-group select { -webkit-appearance: none; -moz-appearance: none; appearance: none; background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M2 5l6 6 6-6'/%3e%3c/svg%3e"); background-repeat: no-repeat; background-position: right 0.75rem center; background-size: 16px 12px; padding-right: 2.5rem; }
.form-group input:focus, .form-group select:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-light); }
.modal-actions { margin-top: 24px; display: flex; justify-content: flex-end; gap: 16px; }
.modal-actions button { padding: 10px 20px; border-radius: 4px; border: 1px solid #ccc; background-color: #fff; cursor: pointer; }
.modal-actions button[type="submit"] { background-color: var(--color-primary); color: white; border-color: var(--color-primary); }
.redesigned-modal { width: 700px; max-width: 90vw; padding: 0; }
.modal-header-redesigned { padding: 24px 32px; border-bottom: 1px solid var(--color-border); }
.modal-header-redesigned h2 { margin: 0 0 4px 0; font-size: 1.6em; font-weight: 600; }
.modal-header-redesigned p { margin: 0; font-size: 1em; color: var(--color-text-secondary); }
.modal-body-redesigned { padding: 24px 32px; max-height: 65vh; overflow-y: auto; }
.form-section { border: none; padding: 0 0 24px 0; margin: 0 0 24px 0; border-bottom: 1px dashed var(--color-border); }
.form-section:last-of-type { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
.form-section legend { font-size: 1.1em; font-weight: 600; margin-bottom: 16px; padding: 0; color: var(--color-primary); }
.form-row { display: flex; gap: 24px; flex-wrap: wrap; }
.form-group.flex-grow { flex: 1 1 0; }
.major-links-container { display: flex; flex-direction: column; gap: 12px; }
.major-link-item-redesigned { display: flex; align-items: center; gap: 12px; background-color: var(--color-background); padding: 12px; border-radius: var(--border-radius); }
.major-link-item-redesigned .major-select { flex-grow: 1; }
.major-link-item-redesigned .type-select { width: 100px; flex-shrink: 0; }
.remove-link-btn-redesigned { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; border: 1px solid var(--color-border); background-color: #fff; color: var(--color-danger); border-radius: 50%; cursor: pointer; transition: all 0.2s ease; }
.remove-link-btn-redesigned:hover { background-color: var(--color-danger); color: #fff; border-color: var(--color-danger); }
.add-link-btn-redesigned { display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%; margin-top: 16px; padding: 10px; border: 2px dashed var(--color-border); background-color: transparent; color: var(--color-text-secondary); font-weight: 500; border-radius: var(--border-radius); cursor: pointer; transition: all 0.2s ease; }
.add-link-btn-redesigned:hover { border-color: var(--color-primary); background-color: var(--color-primary-light); color: var(--color-primary); }
.redesigned-modal .modal-actions { padding: 24px 32px; border-top: 1px solid var(--color-border); background-color: #f8f9fa; }
.redesigned-modal .modal-actions button { padding: 10px 24px; font-size: 1em; font-weight: 500; }
.delete-btn-redesigned { background-color: transparent; color: var(--color-danger); border: none; }
.delete-btn-redesigned:hover { text-decoration: underline; }
.cancel-btn-redesigned { background-color: #fff; border: 1px solid #ccc; color: var(--color-text-primary); }
.cancel-btn-redesigned:hover { background-color: #f1f1f1; }
.submit-btn-redesigned { background-color: var(--color-primary); color: #fff; border: 1px solid var(--color-primary); }
.submit-btn-redesigned:disabled { background-color: #a0c3e2; border-color: #a0c3e2; cursor: not-allowed; }

/* 课程目录管理样式 - 精简版 */
.catalog-modal { width: 1000px; max-width: 95vw; max-height: 90vh; display: flex; flex-direction: column; }
.catalog-list-view { display: flex; flex-direction: column; height: 100%; max-height: 85vh; }
.catalog-header { padding: 28px 32px 24px; border-bottom: 1px solid #e9ecef; background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%); }
.catalog-header-content { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 16px; }
.catalog-title-section h2 { margin: 0 0 6px 0; font-size: 1.75em; font-weight: 700; color: #2c3e50; }
.catalog-subtitle { margin: 0; font-size: 1em; color: #6c757d; font-weight: 400; }
.catalog-header-actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.catalog-search-input { width: 250px; padding: 10px 16px; border: 2px solid #e9ecef; border-radius: 8px; font-size: 0.9em; background-color: #fff; transition: all 0.2s ease; }
.catalog-search-input:focus { border-color: #007bff; box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1); outline: none; }
.catalog-add-btn { display: flex; align-items: center; gap: 8px; padding: 10px 18px; background: linear-gradient(135deg, #007bff 0%, #0056b3 100%); color: white; border: none; border-radius: 8px; font-size: 0.9em; font-weight: 600; cursor: pointer; transition: all 0.2s ease; box-shadow: 0 2px 8px rgba(0, 123, 255, 0.2); white-space: nowrap; }
.catalog-add-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3); }
.catalog-content { flex: 1; padding: 24px 32px; overflow-y: auto; background-color: #fbfcfd; }
.catalog-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 300px; gap: 16px; color: #6c757d; }
.loading-spinner { width: 32px; height: 32px; border: 3px solid #e9ecef; border-top: 3px solid #007bff; border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.catalog-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 300px; gap: 16px; text-align: center; color: #6c757d; }
.empty-icon { color: #dee2e6; }
.catalog-empty h3 { margin: 0; font-size: 1.3em; font-weight: 600; color: #495057; }
.catalog-empty p { margin: 0; font-size: 1em; color: #6c757d; }
.catalog-content-wrapper { display: flex; flex-direction: column; height: 100%; }
.catalog-virtual-list { flex: 1; display: flex; flex-direction: column; gap: 8px; margin-bottom: 20px; }
.catalog-row { background: white; border: 1px solid #e9ecef; border-radius: 8px; transition: all 0.2s ease; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); }
.catalog-row:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); border-color: #007bff; transform: translateY(-1px); }
.catalog-row-content { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; }
.catalog-basic-info { display: flex; align-items: center; gap: 20px; flex: 1; }
.catalog-name-section { display: flex; flex-direction: column; gap: 6px; flex: 1; }
.catalog-name { margin: 0; font-size: 1.1em; font-weight: 600; color: #2c3e50; line-height: 1.3; }
.catalog-code { display: inline-block; padding: 3px 8px; background-color: #e7f3ff; color: #0056b3; font-size: 0.8em; font-weight: 500; border-radius: 4px; font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace; width: fit-content; }
.catalog-credits-section { display: flex; flex-direction: column; align-items: center; gap: 2px; min-width: 60px; }
.credits-label { font-size: 0.75em; color: #6c757d; text-transform: uppercase; letter-spacing: 0.5px; font-weight: 500; }
.credits-value { font-size: 1.3em; font-weight: 700; color: #007bff; }
.catalog-row-actions { display: flex; gap: 8px; flex-shrink: 0; }
.catalog-edit-btn-sm, .catalog-delete-btn-sm { display: flex; align-items: center; gap: 4px; padding: 6px 10px; border: 1px solid; border-radius: 5px; font-size: 0.8em; font-weight: 500; cursor: pointer; transition: all 0.2s ease; }
.catalog-edit-btn-sm { background-color: #e7f5ff; color: #0056b3; border-color: #b3d9ff; }
.catalog-edit-btn-sm:hover { background-color: #cce7ff; border-color: #80bfff; }
.catalog-delete-btn-sm { background-color: #fff2f2; color: #dc3545; border-color: #ffb3b3; }
.catalog-delete-btn-sm:hover { background-color: #ffe6e6; border-color: #ff8080; }
.catalog-pagination { display: flex; justify-content: center; align-items: center; gap: 16px; padding: 16px 0; border-top: 1px solid #e9ecef; background-color: #f8f9fa; border-radius: 0 0 8px 8px; margin-top: auto; }
.catalog-pagination-btn { padding: 8px 16px; border: 1px solid #dee2e6; background-color: #fff; color: #495057; border-radius: 6px; cursor: pointer; font-size: 0.9em; font-weight: 500; transition: all 0.2s; }
.catalog-pagination-btn:hover:not(:disabled) { background-color: #007bff; border-color: #007bff; color: #fff; }
.catalog-pagination-btn:disabled { background-color: #f8f9fa; color: #adb5bd; cursor: not-allowed; }
.catalog-pagination-info { font-size: 0.9em; color: #6c757d; font-weight: 500; }
.catalog-footer { padding: 20px 32px; border-top: 1px solid #e9ecef; background-color: #f8f9fa; display: flex; justify-content: flex-end; }
.catalog-close-btn { padding: 10px 24px; background-color: #6c757d; color: white; border: none; border-radius: 6px; font-size: 0.95em; font-weight: 500; cursor: pointer; transition: all 0.2s ease; }
.catalog-close-btn:hover { background-color: #5a6268; }
.catalog-edit-view { padding: 24px 32px; }
.catalog-edit-header { display: flex; align-items: center; gap: 16px; margin-bottom: 32px; padding-bottom: 20px; border-bottom: 1px solid #e9ecef; }
.back-btn { display: flex; align-items: center; gap: 6px; padding: 8px 12px; background-color: #f8f9fa; color: #6c757d; border: 1px solid #dee2e6; border-radius: 6px; font-size: 0.9em; cursor: pointer; transition: all 0.2s ease; }
.back-btn:hover { background-color: #e9ecef; color: #495057; }
.catalog-edit-header h2 { margin: 0; font-size: 1.5em; font-weight: 600; color: #2c3e50; }
.catalog-form { max-width: 600px; }
.catalog-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px 24px; margin-bottom: 32px; }
.catalog-form-grid .form-group:nth-child(1) { grid-column: span 2; }
.catalog-form .form-group label { display: flex; align-items: center; gap: 4px; margin-bottom: 8px; font-weight: 600; color: #495057; }
.required { color: #dc3545; font-size: 0.9em; }
.catalog-form .form-group input { padding: 12px 16px; border: 2px solid #e9ecef; border-radius: 8px; font-size: 1em; transition: all 0.2s ease; }
.catalog-form .form-group input:focus { border-color: #007bff; box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1); }
.catalog-form-actions { display: flex; justify-content: flex-end; gap: 12px; padding-top: 20px; border-top: 1px solid #e9ecef; }
.catalog-form-actions .cancel-btn { padding: 10px 20px; background-color: #6c757d; color: white; border: none; border-radius: 6px; font-size: 0.95em; font-weight: 500; cursor: pointer; transition: all 0.2s ease; }
.catalog-form-actions .cancel-btn:hover { background-color: #5a6268; }
.catalog-form-actions .submit-btn { padding: 10px 20px; background-color: #007bff; color: white; border: none; border-radius: 6px; font-size: 0.95em; font-weight: 500; cursor: pointer; transition: all 0.2s ease; }
.catalog-form-actions .submit-btn:hover { background-color: #0056b3; }
.catalog-form-actions .submit-btn:disabled { background-color: #a0c3e2; cursor: not-allowed; }

@media (max-width: 1400px) { .catalog-modal { width: 900px; } .catalog-search-input { width: 200px; } }
@media (max-width: 768px) { .catalog-header-content { flex-direction: column; align-items: stretch; } .catalog-header-actions { justify-content: space-between; } .catalog-search-input { width: 100%; max-width: 250px; } .catalog-row-content { flex-direction: column; align-items: flex-start; gap: 12px; } .catalog-basic-info { width: 100%; } .catalog-row-actions { align-self: flex-end; } }
</style>