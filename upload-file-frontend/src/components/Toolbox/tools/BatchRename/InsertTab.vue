<template>
  <div class="grid grid-cols-2 gap-4">
    <div>
      <el-form label-width="80px">
        <el-form-item label="插入文本">
          <div class="flex gap-2">
            <el-select v-model="insertPosition" class="flex-1">
              <el-option label="在开头" value="start"></el-option>
              <el-option label="在结尾" value="end"></el-option>
              <el-option label="在指定位置" value="position"></el-option>
            </el-select>
            <el-input v-model="insertText" placeholder="要插入的文本" class="flex-1"></el-input>
          </div>
        </el-form-item>
        <el-form-item label="替换文本">
          <el-input v-model="searchText" placeholder="查找" class="mb-2"></el-input>
          <el-input v-model="replaceText" placeholder="替换为" class="mb-2"></el-input>
          <el-radio-group v-model="replaceOption">
            <el-radio label="all">全部替换</el-radio>
            <el-radio label="first">仅首次</el-radio>
            <el-radio label="last">仅最后</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>
    <div>
      <el-form label-width="80px">
        <el-form-item label="正则表达式">
          <el-input v-model="regexPattern" placeholder="正则模式" class="mb-2"></el-input>
          <el-input v-model="regexReplace" placeholder="替换为 ($1, $2...)" class="mb-2"></el-input>
          <div class="text-xs text-gray-500">
            使用 $1, $2 等引用捕获组。例如："(.*)\.(.*)" → "$2.$1" 可交换点前后的部分。
          </div>
        </el-form-item>
        <el-form-item label="大小写选项">
          <el-button-group>
            <el-button size="small">全大写</el-button>
            <el-button size="small">全小写</el-button>
            <el-button size="small">标题格式</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      insertPosition: 'start',
      insertText: '',
      searchText: '',
      replaceText: '',
      replaceOption: 'all',
      regexPattern: '',
      regexReplace: ''
    }
  }
}
</script>