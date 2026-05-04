<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属分区" prop="regionId">
        <el-select v-model="queryParams.regionId" placeholder="所属分区" clearable style="width: 240px">
          <el-option
            v-for="item in regionOptions"
            :key="item.regionId"
            :label="item.regionName"
            :value="item.regionId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="服务器名称" prop="serverName">
        <el-input
          v-model="queryParams.serverName"
          placeholder="请输入服务器名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务器ID" prop="serverId">
        <el-input
          v-model="queryParams.serverId"
          placeholder="请输入服务器ID"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务器类型" prop="serverType">
        <el-select v-model="queryParams.serverType" placeholder="服务器类型" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.game_server_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.game_server_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['game:server:add']"
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
          v-hasPermi="['game:server:edit']"
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
          v-hasPermi="['game:server:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['game:server:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-refresh"
          size="mini"
          :disabled="multiple"
          @click="handleBatchRefresh"
          v-hasPermi="['game:server:edit']"
        >刷新数据源</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="serverList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="服务器ID" align="center" prop="serverId" :show-overflow-tooltip="true" />
      <el-table-column label="服务器名称" align="center" prop="serverName" :show-overflow-tooltip="true" />
      <el-table-column label="所属分区" align="center" prop="regionName" :show-overflow-tooltip="true" />
      <el-table-column label="服务器类型" align="center" prop="serverType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.game_server_type" :value="scope.row.serverType"/>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.game_server_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="开服时间" align="center" prop="openTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.openTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="后台地址" align="center" prop="backendUrl" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['game:server:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['game:server:remove']"
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

    <!-- 添加或修改服务器对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="所属分区" prop="regionId">
          <el-select v-model="form.regionId" placeholder="请选择所属分区" style="width: 100%">
            <el-option
              v-for="item in regionOptions"
              :key="item.regionId"
              :label="item.regionName"
              :value="item.regionId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务器ID" prop="serverId">
          <el-input v-model="form.serverId" placeholder="请输入服务器ID" />
        </el-form-item>
        <el-form-item label="服务器名称" prop="serverName">
          <el-input v-model="form.serverName" placeholder="请输入服务器名称" />
        </el-form-item>
        <el-form-item label="服务器类型" prop="serverType">
          <el-radio-group v-model="form.serverType">
            <el-radio
              v-for="dict in dict.type.game_server_type"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.game_server_status"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开服时间" prop="openTime">
          <el-date-picker
            v-model="form.openTime"
            type="datetime"
            placeholder="请选择开服时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="后台地址" prop="backendUrl">
          <el-input v-model="form.backendUrl" placeholder="请输入后台地址" />
        </el-form-item>
        <el-form-item label="游戏库配置" prop="gameDbConfig">
          <el-input v-model="form.gameDbConfig" type="textarea" :rows="3" placeholder="请输入游戏库配置(JSON格式)" />
        </el-form-item>
        <el-form-item label="日志库配置" prop="logDbConfig">
          <el-input v-model="form.logDbConfig" type="textarea" :rows="3" placeholder="请输入日志库配置(JSON格式)" />
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
  </div>
</template>

<script>
import { listServer, getServer, delServer, addServer, updateServer, refreshServerDs } from "@/api/game/server"
import { listRegion } from "@/api/game/region"
import { listFieldByEntity } from "@/api/game/fieldDefine"
import DynamicFields from "@/components/game/DynamicFields"

export default {
  name: "GameServer",
  dicts: ['sys_normal_disable', 'game_server_type', 'game_server_status'],
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
      // 服务器表格数据
      serverList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 分区选项列表
      regionOptions: [],
      // 动态字段定义
      fieldDefines: [],
      // 选中的行数据（含regionId/serverId）
      selectedRows: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        regionId: undefined,
        serverName: undefined,
        serverId: undefined,
        serverType: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        regionId: [
          { required: true, message: "所属分区不能为空", trigger: "change" }
        ],
        serverId: [
          { required: true, message: "服务器ID不能为空", trigger: "blur" }
        ],
        serverName: [
          { required: true, message: "服务器名称不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getAllRegions()
  },
  methods: {
    /** 查询服务器列表 */
    getList() {
      this.loading = true
      listServer(this.queryParams).then(response => {
        this.serverList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询所有分区列表 */
    getAllRegions() {
      listRegion({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.regionOptions = response.rows || []
      })
    },
    /** 查询动态字段定义 */
    getFieldDefines() {
      listFieldByEntity('server').then(res => {
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
        id: undefined,
        regionId: undefined,
        serverId: undefined,
        serverName: undefined,
        serverType: undefined,
        status: "0",
        openTime: undefined,
        backendUrl: undefined,
        gameDbConfig: undefined,
        logDbConfig: undefined,
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
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.getFieldDefines()
      this.open = true
      this.title = "添加服务器"
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.selectedRows = selection
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getServer(id).then(response => {
        this.form = response.data
        if (!this.form.dynamicFields) {
          this.form.dynamicFields = {}
        }
        this.getFieldDefines()
        this.open = true
        this.title = "修改服务器"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (!this.form.dynamicFields) {
            this.form.dynamicFields = {}
          }
          if (this.form.id != undefined) {
            updateServer(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addServer(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 刷新数据源按钮操作 */
    /** 批量刷新数据源 */
    handleBatchRefresh() {
      const rows = this.selectedRows
      if (!rows || rows.length === 0) {
        this.$modal.msgError("请先选择要刷新数据源的服务器")
        return
      }
      const names = rows.map(r => r.serverName).join("、")
      this.$modal.confirm('确认刷新"' + names + '"等 ' + rows.length + ' 台服务器的数据源连接？').then(() => {
        const promises = rows.map(row => refreshServerDs(row.regionId, row.serverId))
        return Promise.all(promises)
      }).then(() => {
        this.$modal.msgSuccess("数据源已刷新")
      }).catch(() => {})
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除服务器编号为"' + ids + '"的数据项？').then(function() {
        return delServer(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('/system/game/server/export', {
        ...this.queryParams
      }, `server_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
