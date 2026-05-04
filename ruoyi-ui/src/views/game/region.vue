<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="所属项目" clearable style="width: 240px" @change="handleProjectChange">
          <el-option
            v-for="item in projectOptions"
            :key="item.projectId"
            :label="item.projectName"
            :value="item.projectId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="所属渠道" prop="channelId">
        <el-select v-model="queryParams.channelId" placeholder="所属渠道" clearable style="width: 240px">
          <el-option
            v-for="item in channelOptions"
            :key="item.channelId"
            :label="item.channelName"
            :value="item.channelId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="分区名称" prop="regionName">
        <el-input
          v-model="queryParams.regionName"
          placeholder="请输入分区名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
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
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['game:region:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['game:region:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['game:region:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['game:region:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="regionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="分区ID" align="center" prop="regionId" />
      <el-table-column label="分区编码" align="center" prop="regionCode" :show-overflow-tooltip="true" />
      <el-table-column label="分区名称" align="center" prop="regionName" :show-overflow-tooltip="true" />
      <el-table-column label="所属项目" align="center" prop="projectName" :show-overflow-tooltip="true" />
      <el-table-column label="所属渠道" align="center" prop="channelName" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="显示顺序" align="center" prop="sort" />
      <el-table-column label="代理分区" align="center" prop="proxyRegionName" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="导出配置" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.exported === 'Y'" type="success" size="small">已导出</el-tag>
          <el-button
            v-else
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleExportConfig(scope.row)"
            v-hasPermi="['game:region:exportConfig']"
          >导出配置</el-button>
        </template>
      </el-table-column>
      <el-table-column label="自动开服" align="center" width="100">
        <template slot-scope="scope">
          <el-tag type="info" size="small">待开发</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="克隆配置" align="center" width="100">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-copy-document"
            @click="handleClone(scope.row)"
            v-hasPermi="['game:region:clone']"
          >克隆</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['game:region:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['game:region:remove']"
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

    <!-- 添加或修改分区对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择所属项目" style="width: 100%" @change="handleFormProjectChange">
            <el-option
              v-for="item in projectOptions"
              :key="item.projectId"
              :label="item.projectName"
              :value="item.projectId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属渠道" prop="channelId">
          <el-select v-model="form.channelId" placeholder="请选择所属渠道" style="width: 100%">
            <el-option
              v-for="item in channelOptions"
              :key="item.channelId"
              :label="item.channelName"
              :value="item.channelId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分区编码" prop="regionCode">
          <el-input v-model="form.regionCode" placeholder="请输入分区编码" />
        </el-form-item>
        <el-form-item label="分区名称" prop="regionName">
          <el-input v-model="form.regionName" placeholder="请输入分区名称" />
        </el-form-item>
        <el-form-item label="代理分区" prop="proxyRegionKey">
          <el-select v-model="form.proxyRegionKey" placeholder="请选择代理分区（不选则不代理）" clearable style="width: 100%">
            <el-option
              v-for="item in proxyRegionOptions"
              :key="item.regionCode"
              :label="item.regionName"
              :value="item.regionCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示顺序" prop="sort">
          <el-input-number v-model="form.sort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <!-- 动态字段 -->
        <DynamicFields
          v-if="fieldDefines.length > 0"
          :fieldDefines="fieldDefines"
          v-model="form.dynamicFields"
          formType="form"
        />
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 克隆分区对话框 -->
    <el-dialog title="克隆分区" :visible.sync="cloneOpen" width="500px" append-to-body>
      <el-form ref="cloneForm" :model="cloneForm" :rules="cloneRules" label-width="120px">
        <el-form-item label="目标渠道" prop="targetChannelId">
          <el-select v-model="cloneForm.targetChannelId" placeholder="请选择目标渠道" style="width: 100%">
            <el-option
              v-for="item in channelOptions"
              :key="item.channelId"
              :label="item.channelName"
              :value="item.channelId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="新分区名称" prop="newName">
          <el-input v-model="cloneForm.newName" placeholder="请输入新分区名称" />
        </el-form-item>
        <el-form-item label="新分区编码" prop="newCode">
          <el-input v-model="cloneForm.newCode" placeholder="请输入新分区编码" />
        </el-form-item>
        <el-form-item label="克隆服务器" prop="cloneServers">
          <el-switch v-model="cloneForm.cloneServers" :active-value="true" :inactive-value="false" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitClone">确 定</el-button>
        <el-button @click="cloneOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRegion, getRegion, delRegion, addRegion, updateRegion, listRegionByChannel, exportConfig, cloneRegion } from "@/api/game/region"
import { listProject } from "@/api/game/project"
import { listChannelByProject } from "@/api/game/channel"
import { listFieldByEntity } from "@/api/game/fieldDefine"
import DynamicFields from "@/components/game/DynamicFields"

export default {
  name: "GameRegion",
  dicts: ['sys_normal_disable'],
  components: { DynamicFields },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 分区表格数据
      regionList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示克隆弹出层
      cloneOpen: false,
      // 克隆的分区ID
      cloneRegionId: undefined,
      // 日期范围
      dateRange: [],
      // 项目选项列表
      projectOptions: [],
      // 渠道选项列表
      channelOptions: [],
      // 代理分区选项列表
      proxyRegionOptions: [],
      // 动态字段定义
      fieldDefines: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        channelId: undefined,
        regionName: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 克隆表单参数
      cloneForm: {
        newName: undefined,
        newCode: undefined,
        targetChannelId: undefined,
        cloneServers: true
      },
      // 表单校验
      rules: {
        channelId: [
          { required: true, message: "所属渠道不能为空", trigger: "change" }
        ],
        regionCode: [
          { required: true, message: "分区编码不能为空", trigger: "blur" }
        ],
        regionName: [
          { required: true, message: "分区名称不能为空", trigger: "blur" }
        ]
      },
      // 克隆表单校验
      cloneRules: {
        targetChannelId: [
          { required: true, message: "目标渠道不能为空", trigger: "change" }
        ],
        newName: [
          { required: true, message: "新分区名称不能为空", trigger: "blur" }
        ],
        newCode: [
          { required: true, message: "新分区编码不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getAllProjects()
  },
  methods: {
    /** 查询分区列表 */
    getList() {
      this.loading = true
      listRegion(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.regionList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询所有项目 */
    getAllProjects() {
      listProject({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.projectOptions = response.rows || []
      })
    },
    /** 项目变更时加载对应渠道 */
    handleProjectChange(projectId) {
      this.channelOptions = []
      this.queryParams.channelId = undefined
      if (projectId) {
        listChannelByProject(projectId).then(response => {
          this.channelOptions = response.data || response.rows || []
        })
      }
    },
    /** 表单中项目变更时加载对应渠道 */
    handleFormProjectChange(projectId) {
      this.channelOptions = []
      this.form.channelId = undefined
      if (projectId) {
        listChannelByProject(projectId).then(response => {
          this.channelOptions = response.data || response.rows || []
        })
      }
    },
    /** 加载代理分区选项列表（排除当前分区） */
    loadProxyRegionOptions(excludeRegionId) {
      listRegion({ pageNum: 1, pageSize: 9999 }).then(response => {
        let list = response.rows || []
        if (excludeRegionId) {
          list = list.filter(item => item.regionId !== excludeRegionId)
        }
        this.proxyRegionOptions = list
      })
    },
    /** 查询动态字段定义 */
    getFieldDefines() {
      listFieldByEntity('region').then(res => {
        this.fieldDefines = res.data
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        regionId: undefined,
        projectId: undefined,
        channelId: undefined,
        regionCode: undefined,
        regionName: undefined,
        proxyRegionKey: undefined,
        status: "0",
        sort: undefined,
        remark: undefined,
        dynamicFields: {}
      }
      this.channelOptions = []
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.getFieldDefines()
      this.getAllProjects()
      this.loadProxyRegionOptions()
      this.open = true
      this.title = "添加分区"
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.regionId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const regionId = row.regionId || this.ids
      getRegion(regionId).then(response => {
        this.form = response.data
        if (!this.form.dynamicFields) {
          this.form.dynamicFields = {}
        }
        // 加载项目列表并选中
        this.getAllProjects()
        // 根据分区的项目加载渠道列表
        if (this.form.projectId) {
          listChannelByProject(this.form.projectId).then(res => {
            this.channelOptions = res.data || res.rows || []
          })
        }
        this.getFieldDefines()
        this.loadProxyRegionOptions(regionId)
        this.open = true
        this.title = "修改分区"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (!this.form.dynamicFields) {
            this.form.dynamicFields = {}
          }
          if (this.form.regionId != undefined) {
            updateRegion(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addRegion(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const regionIds = row.regionId || this.ids
      this.$modal.confirm('是否确认删除分区编号为"' + regionIds + '"的数据项？').then(function() {
        return delRegion(regionIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('/system/game/region/export', {
        ...this.queryParams
      }, `region_${new Date().getTime()}.xlsx`)
    },
    /** 导出配置按钮操作 */
    handleExportConfig(row) {
      this.$modal.confirm('确认导出分区"' + row.regionName + '"的配置？').then(() => {
        exportConfig(row.regionId).then(() => {
          this.$modal.msgSuccess("导出配置成功")
          this.getList()
        })
      })
    },
    /** 克隆按钮操作 */
    handleClone(row) {
      this.cloneForm = {
        newName: '',
        newCode: '',
        targetChannelId: row.channelId,
        cloneServers: true
      }
      this.cloneRegionId = row.regionId
      this.cloneOpen = true
    },
    /** 提交克隆 */
    submitClone() {
      this.$refs["cloneForm"].validate(valid => {
        if (valid) {
          cloneRegion(this.cloneRegionId, this.cloneForm).then(() => {
            this.$modal.msgSuccess("克隆成功")
            this.cloneOpen = false
            this.getList()
          })
        }
      })
    }
  }
}
</script>
