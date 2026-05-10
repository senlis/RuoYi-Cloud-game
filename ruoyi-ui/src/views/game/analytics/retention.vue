<template>
  <div class="app-container">
    <ServerSelector v-model="serverSelection" @query="onQuery" />
    <el-tabs v-model="tab" style="margin-top:10px" type="border-card">
      <el-tab-pane label="按日期" name="date">
        <el-table border :data="dateRows" show-summary v-loading="loading" size="small">
          <el-table-column label="创角日期" prop="create_dt" width="105" />
          <el-table-column label="创角数" prop="total" />
          <el-table-column label="Day1" prop="day_1" :formatter="pct" />
          <el-table-column label="Day2" prop="day_2" :formatter="pct" />
          <el-table-column label="Day3" prop="day_3" :formatter="pct" />
          <el-table-column label="Day4" prop="day_4" :formatter="pct" />
          <el-table-column label="Day5" prop="day_5" :formatter="pct" />
          <el-table-column label="Day6" prop="day_6" :formatter="pct" />
          <el-table-column label="Day7" prop="day_7" :formatter="pct" />
          <el-table-column label="Day14" prop="day_14" :formatter="pct" />
          <el-table-column label="Day30" prop="day_30" :formatter="pct" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="按服务器" name="server">
        <el-table border :data="serverRows" show-summary v-loading="loading" size="small">
          <el-table-column label="服务器" prop="server_id" />
          <el-table-column label="创角数" prop="total" />
          <el-table-column label="Day1" prop="day_1" :formatter="pct" />
          <el-table-column label="Day2" prop="day_2" :formatter="pct" />
          <el-table-column label="Day3" prop="day_3" :formatter="pct" />
          <el-table-column label="Day4" prop="day_4" :formatter="pct" />
          <el-table-column label="Day5" prop="day_5" :formatter="pct" />
          <el-table-column label="Day6" prop="day_6" :formatter="pct" />
          <el-table-column label="Day7" prop="day_7" :formatter="pct" />
          <el-table-column label="Day14" prop="day_14" :formatter="pct" />
          <el-table-column label="Day30" prop="day_30" :formatter="pct" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";
import { getRetentionBatch } from "@/api/game/analytics";

const DAYS = ['day_1','day_2','day_3','day_4','day_5','day_6','day_7','day_14','day_30'];
export default {
  name: "AnalyticsRetention",
  components: { ServerSelector },
  data() { return { loading: false, rows: [], tab: 'date', serverSelection: { projectId: null, serverIds: [] } }; },
  computed: {
    dateRows() { return this.groupBy('create_dt'); },
    serverRows() { return this.groupBy('server_id'); }
  },
  methods: {
    onQuery(sel, range) {
      if (!sel.projectId || !range || range.length !== 2) return;
      this.loading = true;
      getRetentionBatch({ projectId: sel.projectId, serverIds: (sel.serverIds||[]).join(','), beginDate: range[0], endDate: range[1] })
        .then(r => { this.rows = r.data || []; this.loading = false; })
        .catch(() => { this.loading = false; });
    },
    groupBy(key) {
      const map = {};
      this.rows.forEach(r => {
        const k = r[key]; if (!k) return;
        if (!map[k]) { map[k] = { [key]: k, total: 0 }; DAYS.forEach(d => map[k][d] = 0); }
        const m = map[k]; m.total += Number(r.total) || 0;
        DAYS.forEach(d => {
          const v = Number(r[d]) || 0;
          m[d] = m.total > 0 ? (m[d] * (m.total - Number(r.total) || 1) + v * Number(r.total)) / m.total : 0;
        });
      });
      return Object.values(map).sort((a,b) => (a[key]||'') > (b[key]||'') ? 1 : -1);
    },
    pct(r, c, v) { return v != null ? (v * 100).toFixed(2) + '%' : '0.00%'; }
  }
};
</script>
