<template>
  <div id="app-container">
    <header class="app-header">
      <h1>学生管理系统</h1>
      <nav>
        <router-link to="/">学生列表</router-link>
        <!-- router-link to="/students/new" style="margin-left: 10px;">添加学生</router-link -->
      </nav>
    </header>
    <main class="app-main">
      <router-view></router-view>
    </main>
    <footer class="app-footer">
      <p>&copy; {{ new Date().getFullYear() }} 学生管理系统</p>
    </footer>
  </div>
</template>

<script setup>
// No specific script needed for App.vue at this moment
</script>

<style scoped>
#app-container {
  font-family: 'Arial', sans-serif;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-header {
  background-color: #333;
  color: white;
  padding: 15px 30px;
  text-align: center;
}

.app-header h1 {
  margin: 0;
  font-size: 1.8em;
}

.app-header nav {
  margin-top: 10px;
}

.app-header nav a {
  color: white;
  text-decoration: none;
  padding: 5px 10px;
  border-radius: 4px;
}

.app-header nav a:hover,
.app-header nav a.router-link-exact-active {
  background-color: #555;
}

.app-main {
  flex-grow: 1;
  padding: 20px;
}

.app-footer {
  background-color: #f8f8f8;
  color: #333;
  text-align: center;
  padding: 10px;
  border-top: 1px solid #eee;
  font-size: 0.9em;
}
</style>
