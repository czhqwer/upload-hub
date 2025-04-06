<template>
  <div class="file-tree-container">
    <!-- 目录路径输入框和获取按钮 -->
    <div class="input-section">
      <SelectDir 
        v-model="directoryPath" 
        :showFiles="false"
        style="width: 300px; margin-right: 10px;"
      />
      <el-input
        v-model.number="maxDepth"
        type="number"
        placeholder="输入目录深度"
        style="width: 120px; margin-right: 10px;"
        :min="0"
      ></el-input>
      <el-button type="primary" @click="fetchFileTree">获取文件树</el-button>
    </div>

    <!-- 过滤选项 -->
    <div class="filter-section" style="margin-top: 20px;">
      <el-input
        v-model="filterText"
        placeholder="输入关键字过滤"
        style="width: 200px; margin-right: 20px;"
      ></el-input>
      <el-checkbox v-model="showFiles">显示文件</el-checkbox>
      <el-checkbox v-model="showFolders">显示文件夹</el-checkbox>
    </div>

    <!-- 文件树和文本展示区域 -->
    <div class="content-section" style="margin-top: 20px;">
      <!-- 文件树 -->
      <div class="tree-section">
        <el-tree
          :data="fileTreeData"
          :props="defaultProps"
          :filter-node-method="filterNode"
          ref="tree"
          node-key="path"
          default-expand-all
          class="tree-container"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node">
            <i :class="data.folder ? 'el-icon-folder' : 'el-icon-document'"></i>
            <span style="margin-left: 8px;">{{ node.label }}</span>
          </span>
        </el-tree>
      </div>

      <!-- 文本树展示 -->
      <div class="text-section">
        <el-input
          type="textarea"
          v-model="textTree"
          placeholder="文件树文本格式将显示在这里"
          class="text-tree-input"
          readonly
        ></el-input>
      </div>
    </div>
  </div>
</template>

<script>
import { getFileTree } from '@/utils/api';
import SelectDir from '@/components/SelectDir/SelectDir.vue';

export default {
  name: 'FileTreeComponent',
  components: {
    SelectDir
  },
  data() {
    return {
      directoryPath: '',
      filterText: '',
      showFiles: true,
      showFolders: true,
      maxDepth: 3, // 默认深度为 3
      fileTreeData: [],
      textTree: '',
      defaultProps: {
        children: 'children',
        label: 'name',
      },
    };
  },
  watch: {
    directoryPath(newVal) {
      if (newVal) {
        this.fetchFileTree();
      }
    },
    filterText(val) {
      this.$refs.tree.filter(val);
      this.$nextTick(() => {
        this.updateTextTree();
      });
    },
    showFiles() {
      if (this.directoryPath) this.fetchFileTree();
    },
    showFolders() {
      if (this.directoryPath) this.fetchFileTree();
    },
    maxDepth() {
      if (this.directoryPath) this.fetchFileTree(); // 深度变化时重新获取
    },
  },
  methods: {
    async fetchFileTree() {
      if (!this.directoryPath) {
        this.$message.warning('请选择目录路径');
        return;
      }
      try {
        const res = await getFileTree(this.directoryPath, this.showFiles, this.showFolders, this.maxDepth);
        if (res.code === 200) {
          this.fileTreeData = [res.data];
          this.$nextTick(() => {
            this.updateTextTree();
          });
          this.$message.success('文件树获取成功');
        }
      } catch (error) {
        this.$message.error('请求失败，请检查网络或路径');
        console.error(error);
      }
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.name.toLowerCase().includes(value.toLowerCase());
    },
    updateTextTree() {
      const visibleNodes = this.getVisibleNodes();
      this.generateTextTreeFromVisibleNodes(visibleNodes);
    },
    getVisibleNodes() {
      const tree = this.$refs.tree;
      const allNodes = [];
      
      const collectVisibleNodes = (node) => {
        if (!node) return;
        if (node.visible) {
          const nodeData = { ...node.data };
          if (node.childNodes && node.childNodes.length > 0) {
            nodeData.children = [];
            node.childNodes.forEach(child => {
              if (child.visible) {
                nodeData.children.push(collectVisibleNodes(child));
              }
            });
            if (nodeData.children.length === 0) delete nodeData.children;
          }
          return nodeData;
        }
        return null;
      };

      tree.root.childNodes.forEach(rootNode => {
        const visibleNode = collectVisibleNodes(rootNode);
        if (visibleNode) allNodes.push(visibleNode);
      });

      return allNodes.length > 0 ? { name: '', children: allNodes } : { name: '' };
    },
    generateTextTreeFromVisibleNodes(node, level = 0, isLast = false, prefix = '') {
      let result = '';
      if (!node) return result;

      if (level === 0 && !node.name) {
        if (node.children && node.children.length > 0) {
          node.children.forEach((child, index) => {
            const isChildLast = index === node.children.length - 1;
            result += this.generateTextTreeFromVisibleNodes(child, 0, isChildLast, '');
          });
        }
      } else {
        const indent = ' '.repeat(level * 2);
        const connector = isLast ? '└── ' : '├── ';
        result += `${prefix}${indent}${connector}${node.name}\n`;

        if (node.children && node.children.length > 0) {
          const newPrefix = prefix + indent + (isLast ? '    ' : '│   ');
          node.children.forEach((child, index) => {
            const isChildLast = index === node.children.length - 1;
            result += this.generateTextTreeFromVisibleNodes(child, level + 1, isChildLast, newPrefix);
          });
        }
      }

      this.textTree = result.trim();
      return result;
    },
  },
};
</script>

<style scoped>
.file-tree-container {
  padding: 20px;
  height: 400px;
  display: flex;
  flex-direction: column;
}

.input-section,
.filter-section {
  display: flex;
  align-items: center;
}

.content-section {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.tree-section {
  flex: 1;
  overflow: hidden;
  margin-right: 20px;
}

.tree-container {
  height: 100%;
  overflow-y: auto;
}

.text-section {
  flex: 1;
  overflow: hidden;
}

.text-tree-input {
  height: 100%;
}

.text-tree-input >>> .el-textarea__inner {
  height: 100%;
  resize: none;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.5;
  padding: 10px;
  background-color: #fff;
  color: #303133;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: border-color 0.3s, box-shadow 0.3s;
}

.text-tree-input >>> .el-textarea__inner:hover {
  border-color: #c0c4cc;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.text-tree-input >>> .el-textarea__inner::-webkit-scrollbar {
  width: 8px;
}

.text-tree-input >>> .el-textarea__inner::-webkit-scrollbar-thumb {
  background-color: #c1c1c1;
  border-radius: 4px;
}

.text-tree-input >>> .el-textarea__inner::-webkit-scrollbar-thumb:hover {
  background-color: #a8a8a8;
}

.text-tree-input >>> .el-textarea__inner::-webkit-scrollbar-track {
  background-color: #f1f1f1;
  border-radius: 4px;
}

.custom-tree-node {
  display: flex;
  align-items: center;
}
</style>