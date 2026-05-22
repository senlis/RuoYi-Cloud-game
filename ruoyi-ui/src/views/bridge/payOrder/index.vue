<template>
  <div class="app-container">
    <!-- 渠道→分区→服务器 三级多选联动组件 -->
    <ChannelServerSelector ref="channelServerSelector" v-model="channelServerSelection" />

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="订单ID" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="请输入订单ID"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="支付状态" clearable style="width: 240px">
          <el-option label="初始" :value="0" />
          <el-option label="已确认-兑换中" :value="5" />
          <el-option label="兑换成功" :value="10" />
          <el-option label="补单成功" :value="15" />
          <el-option label="兑换失败" :value="20" />
        </el-select>
      </el-form-item>
      <el-form-item label="下单时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd HH:mm:ss"
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
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['bridge:payOrder:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="payOrderList">
      <el-table-column label="订单ID" align="center" prop="orderId" :show-overflow-tooltip="true" />
      <el-table-column label="渠道" align="center" prop="channelKey" />
      <el-table-column label="服务器ID" align="center" prop="serverId" />
      <el-table-column label="玩家ID" align="center" prop="playerId" />
      <el-table-column label="账号ID" align="center" prop="accountId" :show-overflow-tooltip="true" />
      <el-table-column label="玩家名" align="center" prop="roleName" :show-overflow-tooltip="true" />
      <el-table-column label="商品名称" align="center" prop="productName" :show-overflow-tooltip="true" />
      <el-table-column label="金额" align="center" prop="amount" />
      <el-table-column label="游戏币" align="center" prop="gameCurrency" />
      <el-table-column label="支付状态" align="center" prop="payStatus">
        <template slot-scope="scope">
          <el-tag :type="payStatusTag(scope.row.payStatus)">
            {{ payStatusLabel(scope.row.payStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" align="center" prop="orderTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.orderTime) }}</span>
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
import { listPayOrder } from "@/api/bridge/payOrder"
import ChannelServerSelector from "@/components/game/ChannelServerSelector"

export default {
  name: "BridgePayOrder",
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
      // 订单表格数据
      payOrderList: [],
      // 日期范围
      dateRange: [],
      // 渠道→分区→服务器 三级联动选中值
      channelServerSelection: { channelKeys: [], serverIds: [] },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderId: undefined,
        payStatus: undefined
      }
    }
  },
  methods: {
    /** 查询订单列表 */
    getList() {
      const channelKeys = this.channelServerSelection.channelKeys
      const channelKey = channelKeys.length > 0 ? channelKeys[0] : null
      if (!channelKey) {
        this.$modal.msgWarning("请先在渠道/分区/服务器选择器中选渠道")
        return
      }
      this.loading = true
      const params = this.addDateRange({...this.queryParams}, this.dateRange)
      params.channelKey = channelKey
      if (this.channelServerSelection.serverIds.length > 0) {
        params.serverIds = this.channelServerSelection.serverIds.join(',')
      }
      listPayOrder(params).then(response => {
        this.payOrderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 支付状态标签颜色 */
    payStatusTag(status) {
      const map = {
        0: 'info',
        5: 'warning',
        10: 'success',
        15: 'success',
        20: 'danger'
      }
      return map[status] || 'info'
    },
    /** 支付状态文本 */
    payStatusLabel(status) {
      const map = {
        0: '初始',
        5: '已确认-兑换中',
        10: '兑换成功',
        15: '补单成功',
        20: '兑换失败'
      }
      return map[status] || '未知'
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
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
      const params = this.addDateRange({...this.queryParams}, this.dateRange)
      params.channelKey = channelKey
      if (this.channelServerSelection.serverIds.length > 0) {
        params.serverIds = this.channelServerSelection.serverIds.join(',')
      }
      this.download('/system/game/bridge/payOrder/export', params, `payOrder_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
