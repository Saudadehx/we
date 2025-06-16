import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { apiClient } from '@/services/apiService';
import { jwtDecode } from 'jwt-decode';

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token'));
    const router = useRouter();

    const isLoggedIn = computed(() => !!token.value);

    const userRole = computed(() => {
        if (!token.value) return null;
        try {
            const decodedToken = jwtDecode(token.value);
            const authorities = decodedToken.authorities || [];
            if (authorities.some(auth => auth.authority === 'ROLE_ADMIN')) return 'ADMIN';
            if (authorities.some(auth => auth.authority === 'ROLE_TEACHER')) return 'TEACHER';
            if (authorities.some(auth => auth.authority === 'ROLE_STUDENT')) return 'STUDENT';
            return null;
        } catch (error) {
            console.error("Token 解码失败", error);
            logout();
            return null;
        }
    });

    const isAdmin = computed(() => userRole.value === 'ADMIN');
    const isTeacher = computed(() => userRole.value === 'TEACHER');
    const isStudent = computed(() => userRole.value === 'STUDENT');

    function setToken(newToken) {
        token.value = newToken;
        if (newToken) {
            localStorage.setItem('token', newToken);
        } else {
            localStorage.removeItem('token');
        }
    }

    async function login(username, password) {
        try {
            const response = await apiClient.post('/auth/authenticate', { username, password });
            const tokenValue = response.token;
            setToken(tokenValue);

            const decodedToken = jwtDecode(tokenValue);
            const authorities = decodedToken.authorities || [];

            // 根据角色跳转到各自的主页
            if (authorities.some(auth => auth.authority === 'ROLE_ADMIN')) {
                await router.push({ name: 'AdminHome' });
            } else if (authorities.some(auth => auth.authority === 'ROLE_TEACHER')) {
                await router.push({ name: 'TeacherDashboard' });
            } else if (authorities.some(auth => auth.authority === 'ROLE_STUDENT')) {
                await router.push({ name: 'StudentProfile' });
            } else {
                await router.push('/login');
            }

            return true;
        } catch (error) {
            console.error("登录失败:", error);
            return false;
        }
    }

    function logout() {
        setToken(null);
        router.push('/login');
    }

    return {
        token,
        isLoggedIn,
        userRole,
        isAdmin,
        isTeacher,
        isStudent,
        login,
        logout,
    };
});