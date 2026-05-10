<template>
  <div class="app-container">
    <ServerSelector v-model="serverSelection" @query="onQuery" />
    <el-tabs v-model="tab" style="margin-top:10px" type="border-card">
      <el-tab-pane label="按日期" name="date">
        <el-table border :data="dateRows" show-summary v-loading="loading" size="small">
          <el-table-column label="日期" prop="dt" width="105" />
          <el-table-column label="付费人数" prop="paying_users" />
          <el-table-column label="付费次数" prop="orders" />
          <el-table-column label="付费金额" prop="revenue" />
          <el-table-column label="ARPPU" prop="arppu" />
          <el-table-column label="首充人数" prop="first_payers" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="按服务器" name="server">
        <el-table border :data="serverRows" show-summary v-loading="loading" size="small">
          <el-table-column label="服务器" prop="server_id" />
          <el-table-column label="付费人数" prop="paying_users" />
          <el-table-column label="付费次数" prop="orders" />
          <el-table-column label="付费金额" prop="revenue" />
          <el-table-column label="ARPPU" prop="arppu" />
          <el-table-column label="首充人数" prop="first_payers" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";
import { getRevenueDaily } from "@/api/game/analytics";

const NUMS = ['paying_users','orders','revenue','arppu','first_payers'];
export default {
  name: "AnalyticsRecharge",
  components: { ServerSelector },
  data() { return { loading: false, rows: [], tab: 'date', serverSelection: { projectId: null, serverIds: [] } }; },
  computed: {
    dateRows() { return this.groupBy('dt'); },
    serverRows() { return this.groupBy('server_id'); }
  },
  methods: {
    onQuery(sel, range) {
      if (!sel.projectId || !range || range.length !== 2) return;
      this.loading = true;
      getRevenueDaily({ projectId: sel.projectId, serverIds: (sel.serverIds||[]).join(','), beginDate: range[0], endDate: range[1], pageSize: 500 })
        .then(r => { this.rows = r.rows || []; this.loading = false; })
        .catch(() => { this.loading = false; });
    },
    groupBy(key) {
      const map = {};
      this.rows.forEach(r => {
        const k = r[key]; if (!k) return;
        if (!map[k]) { map[k] = { [key]: k }; NUMS.forEach(d => map[k][d] = 0); }
        const m = map[k];
        NUMS.forEach(d => m[d] += Number(r[d]) || 0);
        if (key === 'server_id' && m.paying_users > 0) m.arppu = +(m.revenue / m.paying_users).toFixed(2);
      });
      return Object.values(map).sort((a,b) => (a[key]||'') > (b[key]||'') ? 1 : -1);
    }
  }
};
</script>
