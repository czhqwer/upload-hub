<template>
  <div class="format-convert">
    <el-upload
      drag
      :auto-upload="false"
      :on-change="handleFileChange"
    >
      <i class="el-icon-upload"></i>
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
    </el-upload>
    <el-select v-model="targetFormat" placeholder="选择目标格式" class="format-select">
      <el-option
        v-for="format in formatOptions"
        :key="format.value"
        :label="format.label"
        :value="format.value"
      ></el-option>
    </el-select>
    <el-button type="primary" @click="convert" class="action-btn">转换</el-button>
  </div>
</template>
<script>
export default {
  name: 'FormatConvert',
  data() {
    return {
      targetFormat: '',
      formatOptions: [
        { value: 'pdf', label: 'PDF' },
        { value: 'docx', label: 'Word' },
        { value: 'txt', label: 'TXT' }
      ]
    }
  },
  methods: {
    handleFileChange(file) {
      console.log('待转换文件:', file)
    },
    convert() {
      this.$message.success('格式转换功能待实现')
    }
  }
}
</script>

<style scoped>
.format-convert {
  padding: 10px;
}
.format-select {
  margin: 15px 0;
  width: 200px;
}
.action-btn {
  margin-top: 10px;
}
</style>