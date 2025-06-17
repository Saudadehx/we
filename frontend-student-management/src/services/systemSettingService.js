import { apiClient } from './apiService';

export const apiService = {
    getCourseSelectionStatus() {
        return apiClient.get('/settings/course-selection-status');
    },
    setCourseSelectionStatus(isOpen) {
        return apiClient.post('/settings/course-selection-status', { isOpen });
    }
};