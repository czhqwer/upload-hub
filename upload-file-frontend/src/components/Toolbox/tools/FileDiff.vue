<template>
  <div id="app" class="app">
    <div v-if="!showDiff">
      <div class="input-container">
        <div class="text-area-wrapper">
          <label>原始文本：</label>
          <textarea v-model="oldStr" rows="30" class="text-area"></textarea>
        </div>
        <div class="text-area-wrapper">
          <label>新文本：</label>
          <textarea v-model="newStr" rows="30" class="text-area"></textarea>
        </div>
      </div>
      <div class="button-container">
        <button @click="handleCompare" class="compare-button">开始比对</button>
      </div>
    </div>
    <div v-else class="diff-container">
      <div class="diff-header">
        <button @click="showDiff = false" class="back-button">返回编辑</button>
      </div>
      <div class="diff-content">
        <code-diff-viewer
          :new-content="processedNewStr"
          :old-content="processedOldStr"
          title="文本比对结果"
          :language="'javascript'"
          :theme="'light'"
          :show-line-numbers="true"
          :split-view="true"
          :hide-line-numbers="false"
          :context="3"
        />
      </div>
    </div>
  </div>
</template>

<script>
import CodeDiffViewer from '@jafri/vue-diff-view';
import 'highlight.js/styles/github.css';

export default {
  name: 'FileDiff',
  components: {
    CodeDiffViewer
  },
  data() {
    return {
      showDiff: false,
      oldStr: '',
      newStr: '',
      processedOldStr: '',
      processedNewStr: ''
    };
  },
  methods: {
    processText(text) {
      // 确保文本以换行符结尾
      return text.trim() + '\n';
    },
    handleCompare() {
      this.processedOldStr = this.processText(this.oldStr);
      this.processedNewStr = this.processText(this.newStr);
      this.showDiff = true;
    }
  }
};
</script>

<style scoped>
.app {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 10px;
  box-sizing: border-box;
}

.input-container {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  min-height: 0;
}

.text-area-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.text-area {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-family: monospace;
  resize: vertical;
  max-height: 300px;
  min-height: 100px;
  flex: 1;
}

.button-container {
  text-align: center;
  margin: 10px 0;
  flex-shrink: 0;
}

.compare-button, .back-button {
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.compare-button:hover, .back-button:hover {
  background-color: #66b1ff;
}

.diff-header {
  margin-bottom: 10px;
  flex-shrink: 0;
}

label {
  display: block;
  margin-bottom: 4px;
  font-weight: bold;
  flex-shrink: 0;
}

.diff-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.diff-content {
  flex: 1;
  overflow: auto;
  max-height: calc(100vh - 300px);
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 0;
  padding-bottom: 20px;
}

:deep(.diff-viewer) {
  height: 100%;
  overflow: visible;
  border: none !important;
  margin-bottom: 0 !important;
}

:deep(.diff-viewer .diff-content) {
  max-height: none;
  padding-bottom: 20px;
}

:deep(.diff-viewer .diff-line) {
  min-height: 20px;
  line-height: 20px;
}

:deep(.diff-viewer .diff-line-content) {
  padding: 2px 0;
}

:deep(.container) {
  height: 400px;
  border: none;
}
</style>