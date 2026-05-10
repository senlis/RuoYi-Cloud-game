<template>
  <div class="app-container">
    <ServerSelector v-model="serverSelection" @query="onQuery" />
    <el-tabs v-model="tab" style="margin-top:10px" type="border-card">
      <el-tab-pane label="按日期" name="date">
        <el-table border :data="dateRows" show-summary v-loading="loading" size="small">
          <el-table-column label="创角日期" prop="create_dt" width="105" />
          <el-table-column label="创角数" prop="total" />
          <el-table-column label="LTV1" prop="ltv_1" />
          <el-table-column label="LTV2" prop="ltv_2" />
          <el-table-column label="LTV3" prop="ltv_3" />
          <el-table-column label="LTV4" prop="ltv_4" />
          <el-table-column label="LTV5" prop="ltv_5" />
          <el-table-column label="LTV6" prop="ltv_6" />
          <el-table-column label="LTV7" prop="ltv_7" />
          <el-table-column label="LTV14" prop="ltv_14" />
          <el-table-column label="LTV30" prop="ltv_30" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="按服务器" name="server">
        <el-table border :data="serverRows" show-summary v-loading="loading" size="small">
          <el-table-column label="服务器" prop="server_id" />
          <el-table-column label="创角数" prop="total" />
          <el-table-column label="LTV1" prop="ltv_1" />
          <el-table-column label="LTV2" prop="ltv_2" />
          <el-table-column label="LTV3" prop="ltv_3" />
          <el-table-column label="LTV4" prop="ltv_4" />
          <el-table-column label="LTV5" prop="ltv_5" />
          <el-table-column label="LTV6" prop="ltv_6" />
          <el-table-column label="LTV7" prop="ltv_7" />
          <el-table-column label="LTV14" prop="ltv_14" />
          <el-table-column label="LTV30" prop="ltv_30" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";
import { getLtvBatch } from "@/api/game/analytics";

const DAYS = ['ltv_1','ltv_2','ltv_3','ltv_4','ltv_5','ltv_6','ltv_7','ltv_14','ltv_30'];
export default {
  name: "AnalyticsLtv",
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
      getLtvBatch({ projectId: sel.projectId, serverIds: (sel.serverIds||[]).join(','), beginDate: range[0], endDate: range[1] })
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
    }
  }
};
</script>
