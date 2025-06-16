import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '@/services/apiService' // 我们只从apiService导入最底层的apiClient
import { jwtDecode } from 'jwt-decode'

// defineStore的第一个参数是这个store的唯一ID
export const useAuthStore = defineStore('auth', () => {
    // --- State ---
    // 使用 ref 定义响应式状态，并从 localStorage 初始化 token
    const token = ref(localStorage.getItem('token'))
    const router = useRouter()

    // --- Getters ---
    // 是否已登录
    const isLoggedIn = computed(() => !!token.value)

    // 用户角色
    const userRole = computed(() => {
        if (!token.value) return null
        try {
            const decodedToken = jwtDecode(token.value);
            if (decodedToken.authorities && decodedToken.authorities.some(auth => auth.authority === 'ROLE_ADMIN')) {
                return 'ADMIN'
            }
            return 'STUDENT'
        } catch (error) {
            console.error("Token 解码失败", error);
            // 如果 token 无效，执行登出操作
            logout()
            return null
        }
    })

    // 是否是管理员
    const isAdmin = computed(() => userRole.value === 'ADMIN')

    // --- Actions ---
    function setToken(newToken) {
        token.value = newToken
        if (newToken) {
            localStorage.setItem('token', newToken)
        } else {
            localStorage.removeItem('token')
        }
    }

    // 登录动作
    async function login(username, password) {
        try {
            const response = await apiClient.post('/auth/authenticate', { username, password })
            const tokenValue = response.token;
            setToken(tokenValue) // 设置token

            // 【关键修改】解码Token以获取角色
            const decodedToken = jwtDecode(tokenValue);
            const authorities = decodedToken.authorities || [];

            // 判断角色并跳转
            if (authorities.some(auth => auth.authority === 'ROLE_ADMIN')) {
                await router.push('/dashboard') // 管理员跳转到主页概览
            } else if (authorities.some(auth => auth.authority === 'ROLE_STUDENT')) {
                await router.push('/my-profile') // 学生跳转到个人中心
            } else {
                await router.push('/') // 默认跳转
            }

            return true
        } catch (error) {
            console.error("登录失败:", error)
            return false
        }
    }

    // 登出动作
    function logout() {
        setToken(null) // 清空 token
        router.push('/login') // 跳转到登录页
    }

    // 返回 state, getters, 和 actions
    return {
        token,
        isLoggedIn,
        userRole,
        isAdmin,
        login,
        logout,
    }
})