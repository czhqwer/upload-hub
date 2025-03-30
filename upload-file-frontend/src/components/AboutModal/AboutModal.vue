<template>
  <div v-if="show" class="about-modal" @click="closeOnOutside">
    <div class="about-content" @click.stop>
      <h2>关于</h2>
      <p>这个人很懒，什么都没有留下，只说自己爱吃炸排骨。。。</p>
      <span class="version">版本号：{{ version }}</span>
    </div>
  </div>
</template>

<script>
import packageInfo from "../../../package.json";

export default {
  name: 'AboutModal',
  props: { show: Boolean },
  data() {
    return {
      version: packageInfo.version
    };
  },
  methods: {
    close() {
      this.$emit('close');
    },
    closeOnOutside(event) {
      if (event.target.classList.contains('about-modal')) this.close();
    }
  }
};
</script>

<style scoped>
.about-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.about-content {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  text-align: center;
  position: relative;
}

.about-content h2 {
  margin-top: 0;
  font-size: 1.2rem;
  color: #1976d2;
}

.about-content p {
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

.version {
  right: 10px;
  bottom: 10px;
  font-size: 12px;
  color: #999;
}
</style>