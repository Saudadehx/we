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
          <div
              v-for="offering in getOfferingsAt(dayIndex, timeSlot.id)"
              :key="offering.id"
              class="course-item"
              :class="{ 'is-hovered': offering.id === hoveredOfferingId }"
              :style="{
              borderLeftColor: getOfferingColor(offering),
              backgroundColor: hexToRgba(getOfferingColor(offering), 0.15)
            }"
              @click.stop="onOfferingClick(offering)"
              @mouseenter="$emit('offering-hover', offering.id)"
              @mouseleave="$emit('offering-hover', null)"
          >
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
import { computed } from 'vue';

const props = defineProps({
  offerings: { type: Array, default: () => [] },
  majors: { type: Array, default: () => [] },
  hoveredOfferingId: { type: [Number, null], default: null }
});

const emit = defineEmits(['cell-click', 'offering-click', 'offering-hover']);

const days = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'];
const timeSlots = [
  { id: 1, range: '8:00-9:40' }, { id: 2, range: '10:00-11:40' },
  { id: 3, range: '14:00-15:40' }, { id: 4, range: '16:00-17:40' },
  { id: 5, range: '19:00-20:40' }
];

const hexToRgba = (hex, alpha = 1) => {
  if (!/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
    return `rgba(217, 217, 217, ${alpha})`;
  }
  let c = hex.substring(1).split('');
  if (c.length === 3) {
    c = [c[0], c[0], c[1], c[1], c[2], c[2]];
  }
  c = '0x' + c.join('');
  const r = (c >> 16) & 255;
  const g = (c >> 8) & 255;
  const b = c & 255;
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
};

const majorColors = computed(() => {
  const colors = [ '#a6cee3','#1f78b4','#b2df8a','#33a02c','#fb9a99','#e31a1c','#fdbf6f','#ff7f00','#cab2d6','#6a3d9a' ];
  const colorMap = new Map();
  props.majors.forEach((major, index) => {
    colorMap.set(major.id, colors[index % colors.length]);
  });
  return colorMap;
});

const getOfferingColor = (offering) => {
  if (offering.associatedMajors && offering.associatedMajors.length > 0) {
    return majorColors.value.get(offering.associatedMajors[0].majorId) || '#d9d9d9';
  }
  return '#d9d9d9';
};

const scheduleMap = computed(() => {
  const map = new Map();
  props.offerings.forEach(offering => {
    if (offering.courseDay && offering.courseTime) {
      const key = `${offering.courseDay}-${offering.courseTime}`;
      if (!map.has(key)) map.set(key, []);
      map.get(key).push(offering);
    }
  });
  return map;
});

const getOfferingsAt = (day, time) => scheduleMap.value.get(`${day}-${time}`) || [];
const onCellClick = (day, time) => emit('cell-click', { day, time });
const onOfferingClick = (offering) => emit('offering-click', offering);
</script>

<style scoped>
.timetable {
  display: grid;
  grid-template-columns: 90px repeat(7, 1fr);
  gap: 1px;
  background-color: #e9ecef;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  overflow: hidden;
  min-width: 1100px;
  font-size: 14px;
}

.header-cell {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 8px 4px;
}
.day-header { padding: 12px 4px; }
.time-header { justify-content: space-around; font-size: 12px; }
.time-slot-id { font-size: 1.1em; font-weight: 700; color: var(--color-primary); }
.time-range { color: #6c757d; }

.class-cell {
  background-color: #fff;
  border-top: 1px solid #f1f3f5;
  border-left: 1px solid #f1f3f5;
  padding: 4px;
  min-height: 110px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: stretch;
  cursor: pointer;
  transition: background-color 0.2s;
}
.class-cell:hover { background-color: #f1f9ff; }

.course-item {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border-radius: 6px;
  color: #343a40;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease-in-out;
  /* 边框样式分开写，颜色通过内联样式绑定 */
  border-left-width: 4px;
  border-left-style: solid;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.course-item:hover, .course-item.is-hovered {
  transform: scale(1.03);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 10;
}

.course-details { display: flex; flex-direction: column; gap: 4px; }
.course-details strong { font-size: 0.9em; font-weight: 600; color: #212529; }
.course-details span { font-size: 0.8em; color: #495057; }
.major-tags-container { margin-top: 6px; display: flex; flex-wrap: wrap; gap: 4px; }
.major-tag { background-color: rgba(0,0,0,0.04); padding: 2px 6px; border-radius: 4px; font-size: 0.75em; }
</style>