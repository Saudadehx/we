<template>
  <div class="schedule-grid-container">
    <div class="timetable">
      <div class="header-cell time-col-header">时间</div>
      <div v-for="day in days" :key="day" class="header-cell day-header">{{ day }}</div>

      <template v-for="timeSlot in timeSlots" :key="timeSlot.id">
        <div class="header-cell time-header">
          <div class="time-slot-id">第 {{ timeSlot.id }} 节</div>
          <div class="time-range">{{ timeSlot.range }}</div>
        </div>

        <div v-for="dayIndex in 7" :key="`${timeSlot.id}-${dayIndex}`" class="class-cell" @click="onCellClick(dayIndex, timeSlot.id)">
          <div v-for="offering in getOfferingsAt(dayIndex, timeSlot.id)" :key="offering.id" class="course-item" :style="{ backgroundColor: getOfferingColor(offering) }" @click.stop="onOfferingClick(offering)">
            <div class="course-details">
              <strong>{{ offering.courseName }}</strong>
              <span>{{ offering.teacherName }}</span>
              <div class="major-tags-container" v-if="offering.associatedMajors && offering.associatedMajors.length > 0">
                <span v-for="majorInfo in offering.associatedMajors" :key="majorInfo.majorId" class="major-tag">
                  {{ majorInfo.majorName }}
                </span>
              </div>
            </div>
          </div>
        </div>

      </template>
    </div>
  </div>
</template>

<script setup>
// <script setup> 部分的代码与上一个回复中的版本保持一致，此处不再重复。
// 重要的是，getOfferingsAt 方法返回的是一个数组，这正是我们需要的。
import { computed } from 'vue';

const props = defineProps({
  offerings: { type: Array, default: () => [] },
  majors: { type: Array, default: () => [] },
});

const emit = defineEmits(['cell-click', 'offering-click']);

const days = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'];
const timeSlots = [
  { id: 1, range: '8:00-9:40' },
  { id: 2, range: '10:00-11:40' },
  { id: 3, range: '14:00-15:40' },
  { id: 4, range: '16:00-17:40' },
  { id: 5, range: '19:00-20:40' }
];

const majorColors = computed(() => {
  const colors = ['#8dd3c7', '#ffffb3', '#bebada', '#fb8072', '#80b1d3', '#fdb462', '#b3de69', '#fccde5', '#d9d9d9', '#bc80bd'];
  const colorMap = new Map();
  props.majors.forEach((major, index) => {
    colorMap.set(major.id, colors[index % colors.length]);
  });
  return colorMap;
});

const getOfferingColor = (offering) => {
  if (offering.associatedMajors && offering.associatedMajors.length > 0) {
    const primaryMajorId = offering.associatedMajors[0].majorId;
    return majorColors.value.get(primaryMajorId) || '#e9ecef';
  }
  return '#e9ecef';
};

const scheduleMap = computed(() => {
  const map = new Map();
  props.offerings.forEach(offering => {
    if (offering.courseDay && offering.courseTime) {
      const key = `${offering.courseDay}-${offering.courseTime}`;
      if (!map.has(key)) {
        map.set(key, []);
      }
      map.get(key).push(offering);
    }
  });
  return map;
});

const getOfferingsAt = (day, time) => {
  return scheduleMap.value.get(`${day}-${time}`) || [];
};

const onCellClick = (day, time) => {
  emit('cell-click', { day, time });
};

const onOfferingClick = (offering) => {
  emit('offering-click', offering);
};
</script>

<style scoped>
.timetable {
  display: grid;
  grid-template-columns: 100px repeat(7, minmax(150px, 1fr)); /* 增加最小宽度保证内容显示 */
  gap: 2px;
  background-color: var(--color-border);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  overflow: hidden;
  min-width: 1200px; /* 保证在小屏幕下可滚动 */
}

/* ✨ 修改：这是关键的样式改变 */
.class-cell {
  background-color: #fff;
  padding: 4px; /* 减小内边距，为课程块留出空间 */
  min-height: 100px; /* 增加最小高度 */
  display: flex;
  flex-direction: column; /* 让内部的课程块纵向排列 */
  gap: 4px; /* 课程块之间的间距 */
  align-items: stretch;
  cursor: pointer;
  transition: background-color 0.2s;
}

.class-cell:hover {
  background-color: #f1f3f5;
}

.course-item {
  width: 100%;
  padding: 6px;
  box-sizing: border-box;
  border-radius: 6px;
  color: #333;
  cursor: pointer;
  border: 1px solid rgba(0,0,0,0.1);
  text-align: left;
}
/* 其他样式(.header-cell, .time-header, .course-details, .major-tag 等)保持不变 */
.header-cell {
  background-color: #f8f9fa;
  font-weight: 600;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}
.time-header { justify-content: space-around; }
.time-slot-id { font-size: 1.1em; font-weight: 700; color: var(--color-primary); }
.time-range { font-size: 0.8em; color: var(--color-text-secondary); }
.course-details { display: flex; flex-direction: column; gap: 4px; }
.course-details strong { display: block; font-size: 0.9em; font-weight: 600; }
.course-details span { font-size: 0.8em; color: #555; }
.major-tags-container { margin-top: 4px; display: flex; flex-wrap: wrap; gap: 4px; }
.major-tag { background-color: rgba(0, 0, 0, 0.05); padding: 2px 6px; border-radius: 4px; font-size: 0.75em; font-style: italic; white-space: nowrap; }
</style>