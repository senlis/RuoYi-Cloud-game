<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="渠道标识" prop="channelKey">
        <el-input
          v-model="queryParams.channelKey"
          placeholder="请输入渠道标识"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="区域标识" prop="regionKey">
        <el-input
          v-model="queryParams.regionKey"
          placeholder="请输入区域标识"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="平台名称" prop="platformName">
        <el-input
          v-model="queryParams.platformName"
          placeholder="请输入平台名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
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
          v-hasPermi="['bridge:channelArg:add']"
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
          v-hasPermi="['bridge:channelArg:edit']"
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
          v-hasPermi="['bridge:channelArg:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="channelArgList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="channelId" width="80" />
      <el-table-column label="渠道标识" align="center" prop="channelKey" :show-overflow-tooltip="true" />
      <el-table-column label="区域标识" align="center" prop="regionKey" :show-overflow-tooltip="true" />
      <el-table-column label="平台名称" align="center" prop="platformName" :show-overflow-tooltip="true" width="120" />
      <el-table-column label="包名" align="center" prop="packageName" :show-overflow-tooltip="true" width="140" />
      <el-table-column label="应用ID" align="center" prop="appId" :show-overflow-tooltip="true" width="140" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="60" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['bridge:channelArg:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['bridge:channelArg:remove']"
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

    <!-- 添加或修改渠道出包参数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="渠道标识" prop="channelKey">
              <el-select
                v-model="form.channelKey"
                placeholder="请选择渠道"
                filterable
                clearable
                style="width: 100%"
                @change="handleChannelChange"
              >
                <el-option
                  v-for="item in channelOptions"
                  :key="item.channelCode"
                  :label="item.channelName"
                  :value="item.channelCode"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区域标识" prop="regionKey">
              <el-select
                v-model="form.regionKey"
                placeholder="请选择区域"
                filterable
                clearable
                style="width: 100%"
                :disabled="!form.channelKey"
              >
                <el-option
                  v-for="item in regionOptions"
                  :key="item.regionCode"
                  :label="item.regionName"
                  :value="item.regionCode"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="平台名称" prop="platformName">
              <el-input v-model="form.platformName" placeholder="请输入平台名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包名" prop="packageName">
              <el-input v-model="form.packageName" placeholder="请输入包名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="应用ID" prop="appId">
              <el-input v-model="form.appId" placeholder="请输入应用ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证地址" prop="authUrl">
              <el-input v-model="form.authUrl" placeholder="请输入认证地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="应用密钥" prop="appKey">
          <el-input v-model="form.appKey" type="textarea" :rows="3" placeholder="请输入应用密钥" />
        </el-form-item>
        <el-form-item label="支付密钥" prop="payKey">
          <el-input v-model="form.payKey" type="textarea" :rows="3" placeholder="请输入支付密钥" />
        </el-form-item>
        <el-form-item label="出包参数" prop="packageParams">
          <el-input v-model="form.packageParams" type="textarea" :rows="3" placeholder="JSON格式" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_normal_disable"
                  :key="dict.value"
                  :label="dict.value"
                >{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" controls-position="right" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listChannelArg, getChannelArg, delChannelArg, addChannelArg, updateChannelArg, listChannelOptions, listRegionOptions } from "@/api/bridge/channelArg"

export default {
  name: "ChannelArg",
  dicts: ['sys_normal_disable'],
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
      // 渠道出包参数配置表格数据
      channelArgList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 渠道下拉选项
      channelOptions: [],
      // 区域下拉选项
      regionOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        channelKey: undefined,
        regionKey: undefined,
        platformName: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        channelKey: [
          { required: true, message: "渠道标识不能为空", trigger: "change" }
        ],
        regionKey: [
          { required: true, message: "区域标识不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.loadChannelOptions()
  },
  methods: {
    /** 查询渠道出包参数配置列表 */
    getList() {
      this.loading = true
      listChannelArg(this.queryParams).then(response => {
        this.channelArgList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 加载渠道下拉选项 */
    loadChannelOptions() {
      listChannelOptions().then(response => {
        this.channelOptions = response.data
      })
    },
    /** 加载区域下拉选项 */
    loadRegionOptions(channelCode) {
      if (!channelCode) {
        this.regionOptions = []
        this.form.regionKey = undefined
        return
      }
      listRegionOptions(channelCode).then(response => {
        this.regionOptions = response.data
      })
    },
    /** 渠道切换时加载对应区域 */
    handleChannelChange(channelCode) {
      this.form.regionKey = undefined
      this.loadRegionOptions(channelCode)
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
        channelKey: undefined,
        regionKey: undefined,
        platformName: undefined,
        packageName: undefined,
        appId: undefined,
        appKey: undefined,
        payKey: undefined,
        authUrl: undefined,
        packageParams: undefined,
        status: "0",
        sort: 0,
        remark: undefined
      }
      this.regionOptions = []
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
      this.title = "添加渠道出包参数配置"
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
      getChannelArg(channelId).then(response => {
        this.form = response.data
        // 加载对应区域选项
        if (this.form.channelKey) {
          this.loadRegionOptions(this.form.channelKey)
        }
        this.open = true
        this.title = "修改渠道出包参数配置"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.channelId != undefined) {
            updateChannelArg(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addChannelArg(this.form).then(() => {
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
      this.$modal.confirm('是否确认删除渠道出包参数配置编号为"' + channelIds + '"的数据项？').then(function() {
        return delChannelArg(channelIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>
