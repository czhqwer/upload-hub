<!-- ContextMenu.vue -->
<template>
  <div v-if="visible" class="context-menu" :style="{ top: top + 'px', left: left + 'px' }" @click.stop>
    <div class="menu-item" @click="copyLink">复制链接</div>
    <div class="menu-item" @click="shareFile">共享文件</div>
    <div class="menu-item" @click="deleteFile">删除文件</div>
  </div>
</template>

<script>
export default {
  props: {
    file: {
      type: Object,
      required: true
    },
    top: {
      type: Number,
      required: true
    },
    left: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      visible: false
    };
  },
  watch: {
    file: {
      handler(newFile) {
        if (newFile) {
          this.show();
        } else {
          this.hide();
        }
      },
      immediate: true
    }
  },
  methods: {
    show() {
      this.visible = true;
    },
    hide() {
      this.visible = false;
    },
    copyLink() {
      this.$emit('copy-link', this.file.accessUrl);
      this.hide();
    },
    shareFile() {
      this.$emit('share-file', this.file);
      this.hide();
    },
    deleteFile() {
      this.$emit('delete-file', this.file);
      this.hide();
    },
  }
};
</script>

<style scoped>
.context-menu {
  position: fixed;
  background: linear-gradient(135deg, #ffffff, #f8faff);
  border: 1px solid rgba(64, 158, 255, 0.15);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 50, 150, 0.15);
  z-index: 1000;
  padding: 8px 0;
  min-width: 140px;
  animation: fadeIn 0.2s ease-out;
}

.menu-item {
  padding: 10px 20px;
  font-size: 14px;
  color: #2c3e50;
  cursor: pointer;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1976d2;
  transform: translateX(4px);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>