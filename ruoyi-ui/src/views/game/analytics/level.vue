<template>
  <div class="app-container">
    <ServerSelector v-model="serverSelection" @query="onQuery" />
    <el-row :gutter="20" style="margin-top:12px">
      <el-col :span="12">
        <el-table border :data="levelRows" v-loading="loading" size="small">
          <el-table-column label="等级" prop="new_level" width="80" />
          <el-table-column label="角色数" prop="role_count" />
        </el-table>
      </el-col>
      <el-col :span="12">
        <el-form size="small" :inline="true">
          <el-form-item label="起始等级"><el-input-number v-model="fromLevel" :min="1" size="small" controls-position="right" style="width:80px" /></el-form-item>
          <el-form-item label="目标等级"><el-input-number v-model="toLevel" :min="2" size="small" controls-position="right" style="width:80px" /></el-form-item>
          <el-form-item><el-button size="small" type="primary" @click="loadSpeed">查询升级速度</el-button></el-form-item>
        </el-form>
        <el-table border :data="speedRows" v-loading="loading2" size="small">
          <el-table-column label="服务器" prop="server_id" width="70" />
          <el-table-column label="平均耗时(小时)" prop="avg_hours" />
          <el-table-column label="角色数" prop="role_count" width="80" />
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";
import { getLevelDistribution, getLevelupSpeed } from "@/api/game/analytics";

export default {
  name: "AnalyticsLevel",
  components: { ServerSelector },
  data() { return { loading: false, loading2: false, levelRows: [], speedRows: [], fromLevel: 1, toLevel: 10, sel: null } },
  methods: {
    onQuery(sel, range) {
      this.sel = sel;
      if (!sel.projectId || !range || range.length !== 2) return;
      this.loading = true;
      getLevelDistribution({ projectId: sel.projectId, serverIds: (sel.serverIds||[]).join(','), date: range[1], pageSize: 500 })
        .then(r => { this.levelRows = r.data || []; this.loading = false; })
        .catch(() => { this.loading = false; });
    },
    loadSpeed() {
      if (!this.sel || !this.sel.projectId) return;
      this.loading2 = true;
      getLevelupSpeed({ projectId: this.sel.projectId, serverIds: (this.sel.serverIds||[]).join(','), fromLevel: this.fromLevel, toLevel: this.toLevel })
        .then(r => { this.speedRows = r.data || []; this.loading2 = false; })
        .catch(() => { this.loading2 = false; });
    }
  }
};
</script>
