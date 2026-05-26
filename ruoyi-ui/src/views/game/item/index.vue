<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="项目" prop="projectId">
        <el-select v-model="selectedProject" placeholder="请选择项目" style="width: 240px" @change="onProjectChange">
          <el-option
            v-for="item in projectOptions"
            :key="item.projectId"
            :label="item.projectName"
            :value="item.projectId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="道具ID" prop="itemId">
        <el-input
          v-model="queryParams.itemId"
          placeholder="请输入道具ID"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="道具名称" prop="itemName">
        <el-input
          v-model="queryParams.itemName"
          placeholder="请输入道具名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="handleImport"
          v-hasPermi="['game:item:import']"
        >导入Excel</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple || !selectedProject"
          @click="handleDelete"
          v-hasPermi="['game:item:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-warning"
          size="mini"
          :disabled="!selectedProject"
          @click="handleClear"
          v-hasPermi="['game:item:remove']"
        >清空该项目</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="itemList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="道具ID" align="center" prop="itemId" width="120" sortable />
      <el-table-column label="道具名称" align="center" prop="itemName" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['game:item:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 导入对话框（内置项目选择，与查询项目隔离） -->
    <el-dialog title="导入道具" :visible.sync="importOpen" width="500px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="目标项目">
          <el-select v-model="importProjectId" placeholder="请选择项目" style="width: 100%">
            <el-option
              v-for="item in projectOptions"
              :key="item.projectId"
              :label="item.projectName"
              :value="item.projectId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".xlsx,.xls"
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
        action=""
      >
        <el-button slot="trigger" size="small" type="primary">选择文件</el-button>
        <div slot="tip" class="el-upload__tip" style="margin-top:8px;color:#909399;font-size:12px;">
          仅支持 .xlsx / .xls 格式，Excel 必须有表头，第一列 <b>id</b>（道具ID），第二列 <b>name</b>（道具名称）
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitImport" :loading="importLoading">确定导入</el-button>
        <el-button @click="importOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listItem, importItem, delItem, clearItem } from "@/api/game/item"
import { listProject } from "@/api/game/project"

export default {
  name: "GameItem",
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      itemList: [],
      ids: [],
      single: true,
      multiple: true,
      // 查询用项目
      projectOptions: [],
      selectedProject: null,
      // 导入用项目（与查询隔离）
      importProjectId: null,
      // 导入
      importOpen: false,
      importLoading: false,
      fileList: [],
      importFile: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        itemId: undefined,
        itemName: undefined
      }
    }
  },
  created() {
    this.loadProjects()
  },
  methods: {
    loadProjects() {
      listProject().then(res => {
        this.projectOptions = res.rows || []
      })
    },
    onProjectChange() {
      this.queryParams.projectId = this.selectedProject
      this.handleQuery()
    },
    getList() {
      if (!this.selectedProject) {
        this.itemList = []
        this.total = 0
        return
      }
      this.loading = true
      this.queryParams.projectId = this.selectedProject
      listItem(this.queryParams).then(response => {
        this.itemList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.itemId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleImport() {
      this.importOpen = true
      this.importProjectId = null
      this.fileList = []
      this.importFile = null
    },
    handleFileChange(file) {
      this.importFile = file.raw
      return false
    },
    submitImport() {
      if (!this.importProjectId) {
        this.$modal.msgWarning("请先选择目标项目")
        return
      }
      if (!this.importFile) {
        this.$modal.msgWarning("请先选择文件")
        return
      }
      this.importLoading = true
      importItem(this.importProjectId, this.importFile).then(response => {
        this.$modal.msgSuccess(response.msg)
        this.importOpen = false
        this.getList()
      }).catch(() => {
        this.$modal.msgError("导入失败")
      }).finally(() => {
        this.importLoading = false
      })
    },
    handleDelete(row) {
      if (!this.selectedProject) {
        this.$modal.msgWarning("请先选择项目")
        return
      }
      const itemIds = row ? [row.itemId] : this.ids
      this.$modal.confirm('是否确认删除选中道具？').then(() => {
        const promises = itemIds.map(id => delItem(this.selectedProject, id))
        return Promise.all(promises)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleClear() {
      if (!this.selectedProject) {
        this.$modal.msgWarning("请先选择项目")
        return
      }
      this.$modal.confirm('确定清空该项目下所有道具数据？此操作不可恢复！').then(() => {
        return clearItem(this.selectedProject)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("已清空")
      }).catch(() => {})
    }
  }
}
</script>
