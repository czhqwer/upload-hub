<template>
  <div v-if="show" class="settings-modal" @click="closeOnOutside">
    <div class="settings-content" @click.stop>
      <h2>上传设置</h2>
      <form @submit.prevent="save">
        <div class="form-group">
          <label>允许上传的文件格式（用英文逗号分隔，例如 .jpg,.png）:</label>
          <input v-model="tempAllowedFormats" type="text" placeholder=".jpg,.png,.mp4" />
        </div>
        <div class="form-group">
          <label>最大上传大小（MB）:</label>
          <input v-model.number="tempMaxSizeMB" type="number" min="1" step="1" />
        </div>
        <div class="form-actions">
          <button type="submit">保存</button>
          <button type="button" @click="close">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SettingsModal',
  props: {
    show: Boolean,
    allowedFormats: String,
    maxUploadSize: Number
  },
  data() {
    return {
      tempAllowedFormats: this.allowedFormats,
      tempMaxSizeMB: this.maxUploadSize / (1024 * 1024)
    };
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.tempAllowedFormats = this.allowedFormats;
        this.tempMaxSizeMB = this.maxUploadSize / (1024 * 1024);
      }
    }
  },
  methods: {
    save() {
      this.$emit('save', {
        allowedFormats: this.tempAllowedFormats,
        maxUploadSize: this.tempMaxSizeMB * 1024 * 1024
      });
      this.close();
    },
    close() {
      this.$emit('close');
    },
    closeOnOutside(event) {
      if (event.target.classList.contains('settings-modal')) this.close();
    }
  }
};
</script>

<style scoped>
.settings-modal {
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

.settings-content {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.settings-content h2 {
  margin-top: 0;
  font-size: 1.2rem;
  color: #1976d2;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.form-actions button[type="submit"] {
  background: #1976d2;
  color: white;
}

.form-actions button[type="button"] {
  background: #eee;
  color: #333;
}
</style>