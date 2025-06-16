<template>
  <div class="list-container">
    <div
        v-for="student in students"
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
</template>

<script setup>
// defineProps 用于接收父组件传来的数据
const props = defineProps({
  students: {
    type: Array,
    required: true
  }
});

// defineEmits 用于声明可以发出的事件
const emit = defineEmits(['student-selected']);

const selectStudent = (student) => {
  // 当一个学生被点击时，发出一个 "student-selected" 事件，并把学生对象传出去
  emit('student-selected', student);
};
</script>

<style scoped>
.student-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}
.student-item:hover {
  background-color: #f5f5f5;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}
.student-info {
  display: flex;
  flex-direction: column;
}
.student-info strong {
  font-weight: 600;
}
.student-info span {
  font-size: 0.9em;
  color: #666;
}
</style>