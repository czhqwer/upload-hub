<template>
  <div id="app">
    <div class="app-container">
      <router-view />
    </div>
  </div>
</template>

<script>

export default {
  name: 'App',
  components: {
  }
};
</script>

<style scoped>
#app {
  box-sizing: border-box;
  background: linear-gradient(135deg, #f5f9ff 0%, #e6f0ff 100%);
  height: 100vh;
  padding: 0;
  font-family: 'Segoe UI', system-ui, sans-serif;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  overflow: auto;
  padding-top: 80px;
}

.app-container {
  width: 90%;
  max-width: 1000px;
  margin: 0 auto;
}
</style>

<style>
/* 全局样式 */
body, html {
  margin: 0;
  padding: 0;
  height: 100%;
}
</style>