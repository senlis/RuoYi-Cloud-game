<template>
  <div class="app-container">
    <ServerSelector v-model="serverSelection" @query="onQuery" />
    <el-form size="small" :inline="true" style="margin-top:8px">
      <el-form-item label="道具ID"><el-input v-model="itemId" placeholder="道具ID" clearable style="width:150px" /></el-form-item>
      <el-form-item><el-button type="primary" size="small" @click="doQuery">查询</el-button></el-form-item>
    </el-form>
    <el-table border :data="rows" show-summary v-loading="loading" size="small" style="margin-top:8px">
      <el-table-column label="服务器" prop="server_id" width="65" />
      <el-table-column label="日期" prop="dt" width="105" />
      <el-table-column label="产出" prop="total_output" width="90" />
      <el-table-column label="消耗" prop="total_consume" width="90" />
      <el-table-column label="涉及玩家" prop="players" width="80" />
    </el-table>
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";
import { getItemSummary } from "@/api/game/analytics";

export default {
  name: "AnalyticsItem",
  components: { ServerSelector },
  data() { return { loading: false, rows: [], itemId: null, serverSelection: { projectId: null, serverIds: [] } }; },
  methods: {
    onQuery(sel, range) {
      this.serverSelection = sel;
      this._range = range;
    },
    doQuery() {
      const sel = this.serverSelection;
      if (!sel.projectId || !this._range || !this.itemId) { this.$message.warning("请选择项目、日期范围和道具ID"); return; }
      this.loading = true;
      getItemSummary({ projectId: sel.projectId, serverIds: (sel.serverIds||[]).join(','), itemId: this.itemId, beginDate: this._range[0], endDate: this._range[1], pageSize: 500 })
        .then(r => { this.rows = r.data || []; this.loading = false; })
        .catch(() => { this.loading = false; });
    }
  }
};
</script>
