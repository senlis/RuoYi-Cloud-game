<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="渠道KEY" prop="channelKey">
        <el-input
          v-model="queryParams.channelKey"
          placeholder="请输入渠道KEY"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="平台名称" prop="platformName">
        <el-input
          v-model="queryParams.platformName"
          placeholder="请输入平台名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="平台状态" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
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
          v-hasPermi="['bridge:platform:add']"
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
          v-hasPermi="['bridge:platform:edit']"
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
          v-hasPermi="['bridge:platform:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="platformList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="渠道KEY" align="center" prop="channelKey" :show-overflow-tooltip="true" />
      <el-table-column label="平台名称" align="center" prop="platformName" :show-overflow-tooltip="true" />
      <el-table-column label="平台状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="充值状态" align="center" prop="rechargeStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.rechargeStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="数据库地址" align="center" prop="dbHost" :show-overflow-tooltip="true" />
      <el-table-column label="数据库名" align="center" prop="dbName" :show-overflow-tooltip="true" />
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['bridge:platform:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['bridge:platform:remove']"
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

    <!-- 添加或修改渠道接入平台配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="渠道" prop="channelKey">
          <el-select v-model="form.channelKey" placeholder="请选择渠道" @change="handleChannelChange" style="width: 100%">
            <el-option
              v-for="item in channelOptions"
              :key="item.channelCode"
              :label="item.channelName"
              :value="item.channelCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="平台名称" prop="platformName">
          <el-input v-model="form.platformName" placeholder="自动同步渠道名" />
        </el-form-item>
        <el-form-item label="平台状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="充值状态" prop="rechargeStatus">
          <el-radio-group v-model="form.rechargeStatus">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-divider content-position="center">数据库配置</el-divider>
        <el-form-item label="数据库地址" prop="dbHost">
          <el-input v-model="form.dbHost" placeholder="请输入数据库地址" style="width: 360px" />
        </el-form-item>
        <el-form-item label="数据库端口" prop="dbPort">
          <el-input-number v-model="form.dbPort" :min="1" :max="65535" style="width: 200px" />
        </el-form-item>
        <el-form-item label="数据库名" prop="dbName">
          <el-input v-model="form.dbName" placeholder="请输入数据库名" style="width: 360px" />
        </el-form-item>
        <el-form-item label="数据库用户" prop="dbUser">
          <el-input v-model="form.dbUser" placeholder="请输入数据库用户" style="width: 360px" />
        </el-form-item>
        <el-form-item label="数据库密码" prop="dbPwd">
          <el-input v-model="form.dbPwd" type="textarea" placeholder="请输入数据库密码" style="width: 360px" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleTestConnection" :loading="testLoading">测试连接</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPlatform, getPlatform, delPlatform, addPlatform, updatePlatform, testDbConnection } from "@/api/bridge/platform"
import { listChannelOptions } from "@/api/bridge/channelArg"

export default {
  name: "Platform",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 测试连接加载状态
      testLoading: false,
      // 渠道选项列表
      channelOptions: [],
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
      // 平台表格数据
      platformList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        channelKey: undefined,
        platformName: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        channelKey: [
          { required: true, message: "渠道KEY不能为空", trigger: "blur" }
        ],
        platformName: [
          { required: true, message: "平台名称不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.loadChannelOptions()
  },
  methods: {
    /** 查询平台列表 */
    getList() {
      this.loading = true
      listPlatform(this.queryParams).then(response => {
          this.platformList = response.rows
          this.total = response.total
          this.loading = false
        }
      )
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        platformId: undefined,
        channelKey: undefined,
        platformName: undefined,
        status: "0",
        rechargeStatus: "0",
        dbHost: undefined,
        dbPort: 3306,
        dbName: undefined,
        dbUser: undefined,
        dbPwd: undefined,
        remark: undefined
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
      this.open = true
      this.title = "添加平台配置"
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.platformId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const platformId = row.platformId || this.ids
      getPlatform(platformId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改平台配置"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.platformId != undefined) {
            updatePlatform(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addPlatform(this.form).then(() => {
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
      const platformIds = row.platformId || this.ids
      this.$modal.confirm('是否确认删除渠道KEY为"' + platformIds + '"的数据项？').then(function() {
        return delPlatform(platformIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 加载渠道下拉选项 */
    loadChannelOptions() {
      listChannelOptions().then(response => {
        this.channelOptions = response.data || []
      })
    },
    /** 渠道变更时自动填充平台名称 */
    handleChannelChange(channelCode) {
      const found = this.channelOptions.find(item => item.channelCode === channelCode)
      if (found) {
        this.form.platformName = found.channelName
      }
    },
    /** 测试数据库连接按钮操作 */
    handleTestConnection() {
      this.$refs["form"].validateField("dbHost", (error) => {
        if (error) {
          this.$modal.msgError("请先填写数据库地址")
          return
        }
        this.testLoading = true
        const testData = {
          dbHost: this.form.dbHost,
          dbPort: this.form.dbPort,
          dbName: this.form.dbName,
          dbUser: this.form.dbUser,
          dbPwd: this.form.dbPwd
        }
        testDbConnection(testData).then(response => {
          if (response.code === 200) {
            this.$modal.msgSuccess("数据库连接成功")
          } else {
            this.$modal.msgError(response.msg)
          }
        }).catch(() => {
          this.$modal.msgError("数据库连接失败")
        }).finally(() => {
          this.testLoading = false
        })
      })
    }
  }
}
</script>
