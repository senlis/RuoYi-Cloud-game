<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="渠道编码" prop="channelCode">
        <el-input
          v-model="queryParams.channelCode"
          placeholder="请输入渠道编码"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="渠道名称" prop="channelName">
        <el-input
          v-model="queryParams.channelName"
          placeholder="请输入渠道名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="所属项目" clearable style="width: 240px">
          <el-option
            v-for="item in projectOptions"
            :key="item.projectId"
            :label="item.projectName"
            :value="item.projectId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="渠道类型" prop="channelType">
        <el-select v-model="queryParams.channelType" placeholder="渠道类型" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.game_channel_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['game:channel:add']"
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
          v-hasPermi="['game:channel:edit']"
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
          v-hasPermi="['game:channel:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['game:channel:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="channelList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="渠道ID" align="center" prop="channelId" />
      <el-table-column label="渠道编码" align="center" prop="channelCode" :show-overflow-tooltip="true" />
      <el-table-column label="渠道名称" align="center" prop="channelName" :show-overflow-tooltip="true" />
      <el-table-column label="所属项目" align="center" prop="projectName" :show-overflow-tooltip="true" />
      <el-table-column label="渠道类型" align="center" prop="channelType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.game_channel_type" :value="scope.row.channelType"/>
        </template>
      </el-table-column>
      <el-table-column label="联系人" align="center" prop="contactPerson" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="显示顺序" align="center" prop="sort" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['game:channel:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['game:channel:remove']"
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

    <!-- 添加或修改渠道对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择所属项目" style="width: 100%">
            <el-option
              v-for="item in projectOptions"
              :key="item.projectId"
              :label="item.projectName"
              :value="item.projectId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="渠道编码" prop="channelCode">
          <el-input v-model="form.channelCode" placeholder="请输入渠道编码" />
        </el-form-item>
        <el-form-item label="渠道名称" prop="channelName">
          <el-input v-model="form.channelName" placeholder="请输入渠道名称" />
        </el-form-item>
        <el-form-item label="渠道类型" prop="channelType">
          <el-radio-group v-model="form.channelType">
            <el-radio
              v-for="dict in dict.type.game_channel_type"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
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
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
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
        <!-- SecureKey 管理（仅编辑模式展示） -->
        <SecureKeyManager v-if="form.channelId" :channelId="form.channelId" />
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listChannel, getChannel, delChannel, addChannel, updateChannel } from "@/api/game/channel"
import { listProject } from "@/api/game/project"
import { listFieldByEntity } from "@/api/game/fieldDefine"
import DynamicFields from "@/components/game/DynamicFields"
import SecureKeyManager from "@/components/game/SecureKeyManager"

export default {
  name: "GameChannel",
  dicts: ['sys_normal_disable', 'game_channel_type'],
  components: { DynamicFields, SecureKeyManager },
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
      // 渠道表格数据
      channelList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 项目选项列表
      projectOptions: [],
      // 动态字段定义
      fieldDefines: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        channelCode: undefined,
        channelName: undefined,
        projectId: undefined,
        channelType: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        projectId: [
          { required: true, message: "所属项目不能为空", trigger: "change" }
        ],
        channelCode: [
          { required: true, message: "渠道编码不能为空", trigger: "blur" }
        ],
        channelName: [
          { required: true, message: "渠道名称不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getAllProjects()
  },
  methods: {
    /** 查询渠道列表 */
    getList() {
      this.loading = true
      listChannel(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.channelList = response.rows
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
    /** 查询动态字段定义 */
    getFieldDefines() {
      listFieldByEntity('channel').then(res => {
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
        channelId: undefined,
        projectId: undefined,
        channelCode: undefined,
        channelName: undefined,
        channelType: undefined,
        status: "0",
        contactPerson: undefined,
        contactPhone: undefined,
        sort: undefined,
        remark: undefined,
        dynamicFields: {}
      }
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
      this.open = true
      this.title = "添加渠道"
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.channelId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const channelId = row.channelId || this.ids
      getChannel(channelId).then(response => {
        this.form = response.data
        if (!this.form.dynamicFields) {
          this.form.dynamicFields = {}
        }
        this.getFieldDefines()
        this.open = true
        this.title = "修改渠道"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (!this.form.dynamicFields) {
            this.form.dynamicFields = {}
          }
          if (this.form.channelId != undefined) {
            updateChannel(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addChannel(this.form).then(() => {
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
      const channelIds = row.channelId || this.ids
      this.$modal.confirm('是否确认删除渠道编号为"' + channelIds + '"的数据项？').then(function() {
        return delChannel(channelIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('/system/game/channel/export', {
        ...this.queryParams
      }, `channel_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
