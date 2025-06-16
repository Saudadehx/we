import { ref } from 'vue';

export const notification = ref({
    message: '',
    type: 'success', // 'success' or 'error'
    visible: false,
});

export function showNotification(message, type = 'error', duration = 3000) {
    notification.value = { message, type, visible: true };
    setTimeout(() => {
        notification.value.visible = false;
    }, duration);
}