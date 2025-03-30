<template>
  <div v-if="show" class="settings-modal" @click="closeOnOutside">
    <div class="settings-content" @click.stop>
      <div class="modal-header">
        <h2>上传设置</h2>
        <button class="close-btn" @click="close">×</button>
      </div>

      <form @submit.prevent="save">
        <div class="form-group">
          <label>允许上传的文件格式</label>
          <div class="format-selection">
            <div class="template-section">
              <h3 class="section-title">快速模板</h3>
              <div class="template-presets">
                <button
                  v-for="template in formatTemplates"
                  :key="template.name"
                  type="button"
                  @click.prevent="applyTemplate(template.formats)"
                >
                  {{ template.name }}
                </button>
              </div>
            </div>

            <div class="preset-formats">
              <button
                v-for="format in presetFormats"
                :key="format"
                :class="{ active: selectedFormats.includes(format) }"
                @click.prevent="toggleFormat(format)"
                type="button"
              >
                {{ format }}
              </button>
            </div>

            <div class="custom-format">
              <input
                v-model="customFormatInput"
                type="text"
                placeholder="如 .jpg （键入Enter 添加）"
                @keyup.enter.prevent="addCustomFormat"
                @blur="addCustomFormat"
              />
              <button 
                class="add-btn" 
                @click.prevent="addCustomFormat"
                :disabled="!customFormatInput.trim()"
              >
                添加
              </button>
            </div>

            <div class="selected-section">
              <h3 class="section-title">已选</h3>
              <div class="selected-formats">
                <transition-group name="tag">
                  <span 
                    v-for="(format, index) in selectedFormats" 
                    :key="format"
                    class="format-tag"
                  >
                    {{ format }}
                    <span class="remove" @click="removeFormat(index)">×</span>
                  </span>
                </transition-group>
              </div>
            </div>
          </div>
        </div>

        <div class="form-group size-group">
          <label>最大上传大小（MB）</label>
          <div class="size-input">
            <input 
              v-model.number="tempMaxSizeMB" 
              type="number" 
              min="1" 
              step="1"
              placeholder="大小"
            />
            <span class="unit">MB</span>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="close">取消</button>
          <button type="submit" class="save-btn">保存</button>
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
      presetFormats: [
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg",
        ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm",
        ".mp3", ".wav", ".ogg", ".flac", ".aac",
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf",
        ".zip", ".rar", ".7z", ".tar", ".gz",
        ".csv", ".json", ".xml", ".html"
      ],
      formatTemplates: [
        { name: "仅图片", formats: [".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"] },
        { name: "仅视频", formats: [".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm"] },
        { name: "仅音频", formats: [".mp3", ".wav", ".ogg", ".flac", ".aac"] },
        { name: "文档", formats: [".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf"] },
        { name: "压缩", formats: [".zip", ".rar", ".7z", ".tar", ".gz"] },
        { name: "常用", formats: [".jpg", ".png", ".mp4", ".pdf", ".docx", ".xlsx", ".zip"] },
        { name: "全部", formats: [
          ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg",
          ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm",
          ".mp3", ".wav", ".ogg", ".flac", ".aac",
          ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf",
          ".zip", ".rar", ".7z", ".tar", ".gz",
          ".csv", ".json", ".xml", ".html"
        ] }
      ],
      selectedFormats: [],
      customFormatInput: "",
      tempMaxSizeMB: null
    };
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.selectedFormats = this.allowedFormats ? this.allowedFormats.split(",").map(f => f.trim()) : [];
        this.tempMaxSizeMB = this.maxUploadSize ? Math.round(this.maxUploadSize / (1024 * 1024)) : 10;
        this.customFormatInput = "";
      }
    }
  },
  methods: {
    applyTemplate(formats) {
      this.selectedFormats = [...new Set(formats)];
    },
    toggleFormat(format) {
      const index = this.selectedFormats.indexOf(format);
      if (index === -1) this.selectedFormats.push(format);
      else this.selectedFormats.splice(index, 1);
    },
    addCustomFormat() {
      const format = this.customFormatInput.trim();
      if (!format) return;
      const normalizedFormat = format.startsWith('.') ? format.toLowerCase() : `.${format.toLowerCase()}`;
      if (!this.selectedFormats.includes(normalizedFormat)) this.selectedFormats.push(normalizedFormat);
      this.customFormatInput = "";
    },
    removeFormat(index) {
      this.selectedFormats.splice(index, 1);
    },
    save() {
      if (this.selectedFormats.length === 0) {
        alert('请至少选择一种文件格式');
        return;
      }
      if (this.tempMaxSizeMB <= 0) {
        alert('最大上传大小必须大于0');
        return;
      }
      this.$emit('save', {
        allowedFormats: this.selectedFormats.join(","),
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
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.settings-content {
  background: white;
  border-radius: 8px;
  padding: 16px;
  width: 460px;
  max-height: 88vh;
  overflow-y: auto;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.2rem;
  color: #1a73e8;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: #666;
  cursor: pointer;
  padding: 0;
  width: 20px;
  height: 20px;
}

.close-btn:hover {
  color: #333;
}

.form-group {
  margin-bottom: 12px;
}

.form-group label {
  font-weight: 500;
  color: #444;
  margin-bottom: 4px;
  display: block;
  font-size: 0.95rem;
}

.format-selection {
  background: #f8f9fa;
  padding: 8px;
  border-radius: 6px;
}

.template-section,
.selected-section {
  margin-bottom: 8px;
}

.section-title {
  font-size: 0.95rem;
  color: #555;
  margin: 0 0 6px 0;
  font-weight: 500;
}

.template-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.template-presets button {
  padding: 4px 10px;
  background: #ffffff;
  border: 1px solid #ddd;
  border-radius: 16px;
  cursor: pointer;
  font-size: 0.85rem;
  color: #666;
}

.template-presets button:hover {
  background: #1a73e8;
  color: white;
  border-color: #1a73e8;
}

.preset-formats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.preset-formats button {
  padding: 4px 10px;
  border: 1px solid #ddd;
  border-radius: 16px;
  background: white;
  cursor: pointer;
  font-size: 0.85rem;
}

.preset-formats button:hover {
  border-color: #1a73e8;
}

.preset-formats button.active {
  background: #1a73e8;
  color: white;
  border-color: #1a73e8;
}

.custom-format {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}

.custom-format input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

.custom-format input:focus {
  outline: none;
  border-color: #1a73e8;
}

.custom-format .add-btn {
  padding: 6px 12px;
  background: #1a73e8;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.custom-format .add-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.selected-formats {
  min-height: 20px;
}

.format-tag {
  display: inline-flex;
  align-items: center;
  background: #e8f0fe;
  color: #1a73e8;
  padding: 3px 6px;
  border-radius: 12px;
  margin: 3px;
  font-size: 0.85rem;
}

.format-tag .remove {
  margin-left: 4px;
  cursor: pointer;
  width: 14px;
  height: 14px;
  line-height: 14px;
  text-align: center;
  border-radius: 50%;
}

.format-tag .remove:hover {
  background: rgba(0, 0, 0, 0.1);
}

.size-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.size-input {
  position: relative;
  width: 100px;
}

.size-input input {
  width: 100%;
  padding: 6px 20px 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

.size-input input:focus {
  outline: none;
  border-color: #1a73e8;
}

.size-input .unit {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  font-size: 0.9rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.form-actions button {
  padding: 6px 16px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 0.9rem;
}

.cancel-btn {
  background: #f1f3f4;
  border: none;
  color: #444;
}

.cancel-btn:hover {
  background: #e8eaed;
}

.save-btn {
  background: #1a73e8;
  border: none;
  color: white;
}

.save-btn:hover {
  background: #1557b0;
}

/* 动画 */
.tag-enter-active,
.tag-leave-active {
  transition: all 0.2s ease;
}

.tag-enter-from,
.tag-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>