<template>
  <div class="file-diff">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-input type="textarea" v-model="text1" placeholder="输入第一个文件内容" :rows="8"></el-input>
      </el-col>
      <el-col :span="12">
        <el-input type="textarea" v-model="text2" placeholder="输入第二个文件内容" :rows="8"></el-input>
      </el-col>
    </el-row>

    <el-button type="primary" @click="compare" class="action-btn">比较</el-button>

    <div v-if="diffHtml" v-html="diffHtml" class="diff-container"></div>
  </div>
</template>

<script>
import { createTwoFilesPatch } from 'diff';
import { Diff2Html } from 'diff2html';
import 'diff2html/bundles/css/diff2html.min.css';

export default {
  name: 'FileDiff',
  data() {
    return {
      text1: '',
      text2: '',
      diffHtml: ''
    };
  },
  methods: {
    compare() {
      // 生成 Diff
      const diff = createTwoFilesPatch('File1', 'File2', this.text1, this.text2);

      // 解析 Diff 并生成 HTML
      this.diffHtml = Diff2Html.getPrettyHtml(diff, {
        inputFormat: 'diff',
        outputFormat: 'side-by-side', // 或 "line-by-line"
        drawFileList: false
      });
    }
  }
};
</script>

<style scoped>
.file-diff {
  padding: 10px;
}
.action-btn {
  margin-top: 15px;
}
.diff-container {
  margin-top: 20px;
  border: 1px solid #ddd;
  padding: 10px;
  background: #f8f8f8;
}
</style>
