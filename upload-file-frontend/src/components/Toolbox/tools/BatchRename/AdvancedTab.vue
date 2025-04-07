<template>
  <div class="grid grid-cols-2 gap-4">
    <div>
      <el-form label-width="80px">
        <el-form-item label="随机字符串">
          <div class="grid grid-cols-2 gap-2">
            <el-form-item label="长度" label-width="40px">
              <el-input-number v-model="randomLength" :min="1" :max="32" size="small"></el-input-number>
            </el-form-item>
            <el-form-item label="类型" label-width="40px">
              <el-select v-model="randomType" size="small">
                <el-option label="字母数字混合" value="alphanum"></el-option>
                <el-option label="仅字母" value="alpha"></el-option>
                <el-option label="仅数字" value="num"></el-option>
                <el-option label="自定义字符" value="custom"></el-option>
              </el-select>
            </el-form-item>
          </div>
          <div class="mt-2">
            <el-checkbox v-model="useUpper">大写</el-checkbox>
            <el-checkbox v-model="useLower" class="ml-4">小写</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="日期/时间">
          <div class="flex gap-2">
            <el-select v-model="dateType" class="flex-1">
              <el-option label="创建日期" value="created"></el-option>
              <el-option label="修改日期" value="modified"></el-option>
              <el-option label="当前日期" value="current"></el-option>
            </el-select>
            <el-input v-model="dateFormat" class="flex-1" placeholder="YYYY-MM-DD"></el-input>
          </div>
          <div class="text-xs text-gray-500 mt-1">
            格式：YYYY（年），MM（月），DD（日），HH（时），mm（分），ss（秒）
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div>
      <el-form label-width="80px">
        <el-form-item label="文件元数据">
          <div class="flex gap-2">
            <el-select v-model="metadataType" class="flex-1">
              <el-option label="文件大小" value="size"></el-option>
              <el-option label="MD5 哈希" value="md5"></el-option>
              <el-option label="SHA-1 哈希" value="sha1"></el-option>
              <el-option label="尺寸（图片）" value="dimensions"></el-option>
              <el-option label="时长（媒体）" value="duration"></el-option>
            </el-select>
            <el-select v-model="metadataPosition" class="flex-1">
              <el-option label="在开头" value="start"></el-option>
              <el-option label="在结尾" value="end"></el-option>
              <el-option label="替换整个名称" value="replace"></el-option>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="JS 规则">
          <el-input type="textarea" v-model="jsRule" :rows="4" placeholder="function rename(name, index) { return name.toUpperCase(); }"></el-input>
          <div class="text-xs text-gray-500 mt-1">
            可用变量：name（文件名），ext（扩展名），index（文件编号）
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      randomLength: 8,
      randomType: 'alphanum',
      useUpper: false,
      useLower: true,
      dateType: 'created',
      dateFormat: 'YYYY-MM-DD',
      metadataType: 'size',
      metadataPosition: 'start',
      jsRule: ''
    }
  }
}
</script>