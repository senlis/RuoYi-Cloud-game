<template>
  <div class="app-container">
    <!-- 渠道→分区→服务器 三级多选联动组件 -->
    <ChannelServerSelector ref="channelServerSelector" v-model="channelServerSelection" />

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="平台标识" prop="identityId">
        <el-input
          v-model="queryParams.identityId"
          placeholder="请输入平台唯一标识"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="渠道用户ID" prop="channelUserId">
        <el-input
          v-model="queryParams.channelUserId"
          placeholder="请输入渠道用户ID"
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
          v-hasPermi="['bridge:user:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList">
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="平台标识" align="center" prop="identityId" :show-overflow-tooltip="true" />
      <el-table-column label="渠道" align="center" prop="channelKey" />
      <el-table-column label="渠道用户ID" align="center" prop="channelUserId" :show-overflow-tooltip="true" />
      <el-table-column label="服务器ID" align="center" prop="serverId" />
      <el-table-column label="角色ID" align="center" prop="playerId" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最后登录时间" align="center" prop="lastLoginTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastLoginTime) }}</span>
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
import { listUser } from "@/api/bridge/user"
import ChannelServerSelector from "@/components/game/ChannelServerSelector"

export default {
  name: "BridgeUser",
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
      // 用户表格数据
      userList: [],
      // 渠道→分区→服务器 三级联动选中值
      channelServerSelection: { channelKeys: [], serverIds: [] },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        identityId: undefined,
        channelUserId: undefined
      }
    }
  },
  methods: {
    /** 查询用户列表 */
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
      listUser(params).then(response => {
        this.userList = response.rows
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
      this.download('/system/game/bridge/user/export', params, `user_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
