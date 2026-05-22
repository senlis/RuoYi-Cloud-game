<template>
  <div class="app-container">
    <!-- 渠道→分区→服务器 三级多选联动组件 -->
    <ChannelServerSelector ref="channelServerSelector" v-model="channelServerSelection" />

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="角色ID" prop="playerId">
        <el-input
          v-model="queryParams.playerId"
          placeholder="请输入角色ID"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="角色名" prop="roleName">
        <el-input
          v-model="queryParams.roleName"
          placeholder="请输入角色名"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="账号ID" prop="accountId">
        <el-input
          v-model="queryParams.accountId"
          placeholder="请输入账号ID"
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
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['bridge:role:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="roleList">
      <el-table-column label="角色ID" align="center" prop="playerId" />
      <el-table-column label="渠道" align="center" prop="channelKey" />
      <el-table-column label="账号ID" align="center" prop="accountId" :show-overflow-tooltip="true" />
      <el-table-column label="角色名" align="center" prop="roleName" :show-overflow-tooltip="true" />
      <el-table-column label="服务器ID" align="center" prop="serverId" />
      <el-table-column label="等级" align="center" prop="level" />
      <el-table-column label="职业" align="center" prop="vocation" />
      <el-table-column label="VIP" align="center" prop="vip" />
      <el-table-column label="战斗力" align="center" prop="fight" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
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
  </div>
</template>

<script>
import { listRole } from "@/api/bridge/role"
import ChannelServerSelector from "@/components/game/ChannelServerSelector"

export default {
  name: "BridgeRole",
  components: { ChannelServerSelector },
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层（默认不加载，用户点击搜索才查询）
      loading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 角色表格数据
      roleList: [],
      // 渠道→分区→服务器 三级联动选中值
      channelServerSelection: { channelKeys: [], serverIds: [] },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        playerId: undefined,
        roleName: undefined,
        accountId: undefined
      }
    }
  },
  methods: {
    /** 查询角色列表 */
    getList() {
      const channelKeys = this.channelServerSelection.channelKeys
      const channelKey = channelKeys.length > 0 ? channelKeys[0] : null
      if (!channelKey) {
        this.$modal.msgWarning("请先在渠道/分区/服务器选择器中选渠道")
        return
      }
      this.loading = true
      const params = { ...this.queryParams }
      params.channelKey = channelKey
      if (this.channelServerSelection.serverIds.length > 0) {
        params.serverIds = this.channelServerSelection.serverIds.join(',')
      }
      listRole(params).then(response => {
        this.roleList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.channelServerSelection = { channelKeys: [], serverIds: [] }
      if (this.$refs.channelServerSelector) {
        this.$refs.channelServerSelector.resetAll()
      }
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 导出按钮操作 */
    handleExport() {
      const channelKeys = this.channelServerSelection.channelKeys
      const channelKey = channelKeys.length > 0 ? channelKeys[0] : null
      if (!channelKey) {
        this.$modal.msgWarning("请先在渠道/分区/服务器选择器中选渠道")
        return
      }
      const params = { ...this.queryParams }
      params.channelKey = channelKey
      if (this.channelServerSelection.serverIds.length > 0) {
        params.serverIds = this.channelServerSelection.serverIds.join(',')
      }
      this.download('/system/game/bridge/role/export', params, `role_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
