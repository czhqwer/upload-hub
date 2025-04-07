<template>
  <div class="grid grid-cols-2 gap-4">
    <div>
      <el-form label-width="80px">
        <el-form-item label="移除内容">
          <div class="flex flex-wrap gap-2">
            <el-button size="small" @click="removeParentheses">移除括号 () 中的文本</el-button>
            <el-button size="small" @click="removeBrackets">移除方括号 [] 中的文本</el-button>
            <el-button size="small" @click="removeBraces">移除大括号 {} 中的文本</el-button>
            <el-button size="small" @click="removeAllBrackets">移除所有括号</el-button>
          </div>
        </el-form-item>
        <el-form-item label="修剪选项">
          <div class="flex flex-wrap gap-2">
            <el-button size="small" @click="trimSpaces">修剪空格</el-button>
            <el-button size="small" @click="trimUnderscores">修剪下划线</el-button>
            <el-button size="small" @click="trimDots">修剪点号</el-button>
            <el-button size="small" @click="trimAll">修剪所有</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div>
      <el-form label-width="80px">
        <el-form-item label="替换字符">
          <div class="flex flex-wrap gap-2">
            <el-button size="small" @click="spaceToUnderscore">空格转为下划线</el-button>
            <el-button size="small" @click="underscoreToSpace">下划线转为空格</el-button>
            <el-button size="small" @click="spaceToDot">空格转为点号</el-button>
            <el-button size="small" @click="dotToSpace">点号转为空格</el-button>
          </div>
        </el-form-item>
        <el-form-item label="重复字符">
          <div class="flex flex-wrap gap-2">
            <el-button size="small" @click="removeDupSpaces">移除重复空格</el-button>
            <el-button size="small" @click="removeDupUnderscores">移除重复下划线</el-button>
            <el-button size="small" @click="removeDupDots">移除重复点号</el-button>
            <el-button size="small" @click="removeAllDup">移除所有重复</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    removeParentheses() { this.$message.info('移除括号内容（模拟操作）') },
    removeBrackets() { this.$message.info('移除方括号内容（模拟操作）') },
    removeBraces() { this.$message.info('移除大括号内容（模拟操作）') },
    removeAllBrackets() { this.$message.info('移除所有括号（模拟操作）') },
    trimSpaces() { this.$message.info('修剪空格（模拟操作）') },
    trimUnderscores() { this.$message.info('修剪下划线（模拟操作）') },
    trimDots() { this.$message.info('修剪点号（模拟操作）') },
    trimAll() { this.$message.info('修剪所有（模拟操作）') },
    spaceToUnderscore() { this.$message.info('空格转为下划线（模拟操作）') },
    underscoreToSpace() { this.$message.info('下划线转为空格（模拟操作）') },
    spaceToDot() { this.$message.info('空格转为点号（模拟操作）') },
    dotToSpace() { this.$message.info('点号转为空格（模拟操作）') },
    removeDupSpaces() { this.$message.info('移除重复空格（模拟操作）') },
    removeDupUnderscores() { this.$message.info('移除重复下划线（模拟操作）') },
    removeDupDots() { this.$message.info('移除重复点号（模拟操作）') },
    removeAllDup() { this.$message.info('移除所有重复（模拟操作）') }
  }
}
</script>