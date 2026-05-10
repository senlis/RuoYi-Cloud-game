<template>
  <div class="app-container">
    <!-- 筛选区：含四级选服 + 日期 + 查询按钮 -->
    <ServerSelector v-model="serverSelection" @query="onQuery" />

    <el-tabs v-model="activeTab" @tab-click="handleTabClick" type="border-card">
      <!-- ==================== 用户规模 ==================== -->
      <el-tab-pane label="用户规模" name="user">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <div slot="header"><span>新增创角趋势</span></div>
              <el-table v-loading="loading.newRole" :data="data.newRole" size="small" max-height="350">
                <el-table-column prop="dt" label="日期" width="120" />
                <el-table-column prop="server_id" label="服务器" width="80" />
                <el-table-column prop="new_roles" label="新增创角" />
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <div slot="header"><span>DAU 趋势</span></div>
              <el-table v-loading="loading.dau" :data="data.dau" size="small" max-height="350">
                <el-table-column prop="dt" label="日期" width="120" />
                <el-table-column prop="server_id" label="服务器" width="80" />
                <el-table-column prop="dau" label="DAU" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>
        <el-card style="margin-top:15px">
          <div slot="header"><span>留存率</span></div>
          <el-table v-loading="loading.retention" :data="data.retention" size="small" max-height="300">
            <el-table-column prop="create_dt" label="注册日期" width="120" />
            <el-table-column prop="total" label="注册人数" width="100" />
            <el-table-column prop="day_1" label="次日留存" width="100" />
            <el-table-column prop="day_3" label="3日留存" width="100" />
            <el-table-column prop="day_7" label="7日留存" width="100" />
            <el-table-column prop="day_14" label="14日留存" width="100" />
            <el-table-column prop="day_30" label="30日留存" width="100" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ==================== 付费分析 ==================== -->
      <el-tab-pane label="付费分析" name="pay">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <div slot="header"><span>日流水</span></div>
              <el-table v-loading="loading.revenue" :data="data.revenue" size="small" max-height="350">
                <el-table-column prop="dt" label="日期" width="120" />
                <el-table-column prop="server_id" label="服务器" width="80" />
                <el-table-column prop="revenue" label="流水(元)" />
                <el-table-column prop="paying_users" label="付费人数" width="90" />
                <el-table-column prop="arppu" label="ARPPU" width="80" />
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <div slot="header"><span>LTV</span></div>
              <el-table v-loading="loading.ltv" :data="data.ltv" size="small" max-height="350">
                <el-table-column prop="create_dt" label="注册日期" width="120" />
                <el-table-column prop="cohort_size" label="队列人数" width="90" />
                <el-table-column prop="ltv_1" label="LTV1" width="80" />
                <el-table-column prop="ltv_3" label="LTV3" width="80" />
                <el-table-column prop="ltv_7" label="LTV7" width="80" />
                <el-table-column prop="ltv_14" label="LTV14" width="80" />
                <el-table-column prop="ltv_30" label="LTV30" width="80" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>
        <el-card style="margin-top:15px">
          <div slot="header"><span>首充统计</span></div>
          <el-table v-loading="loading.firstRecharge" :data="data.firstRecharge" size="small" max-height="300">
            <el-table-column prop="dt" label="日期" width="120" />
            <el-table-column prop="server_id" label="服务器" width="80" />
            <el-table-column prop="first_payers" label="首充人数" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ==================== 等级分析 ==================== -->
      <el-tab-pane label="等级分析" name="level">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <div slot="header"><span>等级分布</span></div>
              <el-table v-loading="loading.levelDist" :data="data.levelDist" size="small" max-height="400">
                <el-table-column prop="new_level" label="等级" width="80" />
                <el-table-column prop="role_count" label="角色数" />
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <div slot="header">
                <span>升级速度</span>
                <el-input-number v-model="queryParams.fromLevel" :min="1" size="mini" style="width:70px;margin:0 5px" placeholder="起始" />
                →
                <el-input-number v-model="queryParams.toLevel" :min="2" size="mini" style="width:70px;margin:0 5px" placeholder="目标" />
                <el-button size="mini" type="primary" @click="loadLevelupSpeed">查询</el-button>
              </div>
              <el-table v-loading="loading.levelupSpeed" :data="data.levelupSpeed" size="small" max-height="350">
                <el-table-column prop="server_id" label="服务器" width="80" />
                <el-table-column prop="avg_hours" label="平均耗时(小时)" />
                <el-table-column prop="role_count" label="角色数" width="80" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- ==================== 道具经济 ==================== -->
      <el-tab-pane label="道具经济" name="item">
        <el-form :inline="true" size="small">
          <el-form-item label="道具ID">
            <el-input v-model="queryParams.itemId" placeholder="道具ID" style="width:150px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="loadItemSummary">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="loading.item" :data="data.item" size="small" max-height="400">
          <el-table-column prop="dt" label="日期" width="120" />
          <el-table-column prop="server_id" label="服务器" width="80" />
          <el-table-column prop="total_output" label="产出" />
          <el-table-column prop="total_consume" label="消耗" />
          <el-table-column prop="players" label="涉及玩家" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";
import {
  getNewRoleDaily, getDauDaily, getRetention,
  getRevenueDaily, getLtv, getFirstRechargeRate,
  getLevelDistribution, getLevelupSpeed,
  getItemSummary
} from "@/api/game/analytics";

export default {
  name: "AnalyticsDashboard",
  components: { ServerSelector },
  data() {
    return {
      activeTab: "user",
      serverSelection: { projectId: null, serverIds: [] },
      queryParams: {
        itemId: undefined,
        fromLevel: 1,
        toLevel: 10
      },
      loading: {
        newRole: false, dau: false, retention: false,
        revenue: false, ltv: false, firstRecharge: false,
        levelDist: false, levelupSpeed: false,
        item: false
      },
      data: {
        newRole: [], dau: [], retention: [],
        revenue: [], ltv: [], firstRecharge: [],
        levelDist: [], levelupSpeed: [],
        item: []
      }
    };
  },
  created() {},
  methods: {
    onQuery(selection, dateRange) {
      // selection: { projectId, serverIds }, dateRange: [beginDate, endDate]
      this.serverSelection = selection;
      this.loadActiveTab(dateRange);
    },
    handleTabClick() {
      // 留空，让用户手动点查询
    },
    loadActiveTab(dateRange) {
      if (!dateRange || dateRange.length !== 2) return;
      const b = dateRange[0], e = dateRange[1];
      const pid = this.serverSelection.projectId || '';
      const sids = (this.serverSelection.serverIds || []).join(',');

      switch (this.activeTab) {
        case "user":
          this.loadNewRole(pid, sids, b, e); this.loadDau(pid, sids, b, e); this.loadRetention(pid, sids, b);
          break;
        case "pay":
          this.loadRevenue(pid, sids, b, e); this.loadLtv(pid, sids, b); this.loadFirstRecharge(pid, sids, b, e);
          break;
        case "level":
          this.loadLevelDist(pid, sids, b); this.loadLevelupSpeed(pid, sids);
          break;
        case "item":
          if (this.queryParams.itemId) this.loadItemSummary(pid, sids, b, e);
          break;
      }
    },

    loadNewRole(pid, sids, b, e) {
      this.loading.newRole = true;
      getNewRoleDaily({ projectId: pid, serverIds: sids, beginDate: b, endDate: e, pageSize: 500 })
        .then(r => { this.data.newRole = r.rows || []; }).finally(() => { this.loading.newRole = false; });
    },
    loadDau(pid, sids, b, e) {
      this.loading.dau = true;
      getDauDaily({ projectId: pid, serverIds: sids, beginDate: b, endDate: e, pageSize: 500 })
        .then(r => { this.data.dau = r.rows || []; }).finally(() => { this.loading.dau = false; });
    },
    loadRetention(pid, sids, b) {
      this.loading.retention = true;
      getRetention({ projectId: pid, serverIds: sids, createDate: b, gapDays: "1,3,7,14,30" })
        .then(r => { this.data.retention = r.data || []; }).finally(() => { this.loading.retention = false; });
    },
    loadRevenue(pid, sids, b, e) {
      this.loading.revenue = true;
      getRevenueDaily({ projectId: pid, serverIds: sids, beginDate: b, endDate: e, pageSize: 500 })
        .then(r => { this.data.revenue = r.rows || []; }).finally(() => { this.loading.revenue = false; });
    },
    loadLtv(pid, sids, b) {
      this.loading.ltv = true;
      getLtv({ projectId: pid, serverIds: sids, createDate: b, gapDays: "1,3,7,14,30" })
        .then(r => { this.data.ltv = r.data || []; }).finally(() => { this.loading.ltv = false; });
    },
    loadFirstRecharge(pid, sids, b, e) {
      this.loading.firstRecharge = true;
      getFirstRechargeRate({ projectId: pid, serverIds: sids, beginDate: b, endDate: e, pageSize: 500 })
        .then(r => { this.data.firstRecharge = r.rows || []; }).finally(() => { this.loading.firstRecharge = false; });
    },
    loadLevelDist(pid, sids, b) {
      this.loading.levelDist = true;
      getLevelDistribution({ projectId: pid, serverIds: sids, date: b, pageSize: 500 })
        .then(r => { this.data.levelDist = r.data || []; }).finally(() => { this.loading.levelDist = false; });
    },
    loadLevelupSpeed(pid, sids) {
      const p = this.queryParams;
      if (!p.fromLevel || !p.toLevel) return;
      this.loading.levelupSpeed = true;
      getLevelupSpeed({ projectId: pid, serverIds: sids, fromLevel: p.fromLevel, toLevel: p.toLevel, pageSize: 500 })
        .then(r => { this.data.levelupSpeed = r.data || []; }).finally(() => { this.loading.levelupSpeed = false; });
    },
    loadItemSummary(pid, sids, b, e) {
      const p = this.queryParams;
      if (!p.itemId) return;
      this.loading.item = true;
      getItemSummary({ projectId: pid, serverIds: sids, itemId: p.itemId, beginDate: b, endDate: e, pageSize: 500 })
        .then(r => { this.data.item = r.data || []; }).finally(() => { this.loading.item = false; });
    }
  }
};
</script>
