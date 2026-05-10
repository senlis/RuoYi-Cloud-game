<template>
  <div class="app-container">
    <ServerSelector v-model="serverSelection" @query="onQuery" />

    <el-form size="small" :inline="true" style="margin-top:8px">
      <el-form-item label="角色">
        <el-input v-model="queryParams.roleSearch" placeholder="角色ID/角色名" clearable style="width:150px" />
      </el-form-item>
      <el-form-item label="事件类型">
        <el-select v-model="queryParams.eventTypes" multiple collapse-tags filterable
                   placeholder="事件类型" clearable style="width:380px">
          <el-checkbox v-model="eventAll" :indeterminate="eventIndeterminate"
                       size="small" style="display:block;padding:4px 16px" @change="toggleEventAll">全选</el-checkbox>
          <el-option v-for="e in eventTypeOptions" :key="e.eventType"
                     :label="e.eventName + ' (' + e.eventType + ')'" :value="e.eventType" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="eventLogList" border style="margin-top:4px">
      <el-table-column label="时间" align="center" prop="event_time" width="155" />
      <el-table-column label="服务器" align="center" prop="server_id" width="70" />
      <el-table-column label="角色ID" align="center" prop="role_id" width="80" />
      <el-table-column label="角色名" align="center" prop="role_name" width="100" />
      <el-table-column label="等级" align="center" prop="level" width="50" />
      <el-table-column label="VIP" align="center" prop="vip" width="50" />
      <el-table-column label="战力" align="center" prop="fight" width="80" />
      <el-table-column label="事件" align="center" width="110">
        <template slot-scope="scope">{{ eventName(scope.row.event_type) }}</template>
      </el-table-column>
      <el-table-column label="事件详情（语义解析）" align="left" min-width="280">
        <template slot-scope="scope">
          <span class="el-parsed">{{ parseEvent(scope.row) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";
import { getEventLog, listEventType } from "@/api/game/analytics";

export default {
  name: "EventLog",
  components: { ServerSelector },
  data() {
    return {
      loading: false, total: 0, eventLogList: [],
      serverSelection: { projectId: null, serverIds: [] },
      eventTypeOptions: [],
      eventConfigMap: {},   // eventType → { eventName, paramDefine }
      eventAll: false, eventIndeterminate: false,
      queryParams: { pageNum: 1, pageSize: 10, roleSearch: undefined, eventTypes: [] }
    };
  },
  watch: {
    "queryParams.eventTypes"(v) {
      this.eventAll = v.length > 0 && v.length === this.eventTypeOptions.length;
      this.eventIndeterminate = v.length > 0 && v.length < this.eventTypeOptions.length;
    }
  },
  created() {
    this.loadEventTypes();
    this.loadEventConfigs();
  },
  methods: {
    loadEventTypes() {
      listEventType({ pageSize: 200 }).then(r => { this.eventTypeOptions = r.rows || []; });
    },
    loadEventConfigs() {
      // 获取所有事件类型的 param_define 配置
      listEventType({ pageSize: 200 }).then(r => {
        const map = {};
        (r.rows || []).forEach(e => {
          let define = {};
          try { define = JSON.parse(e.paramDefine || '{}'); } catch(ex) {}
          map[e.eventType] = { eventName: e.eventName, paramDefine: define };
        });
        this.eventConfigMap = map;
      });
    },
    toggleEventAll(v) {
      this.queryParams.eventTypes = v ? this.eventTypeOptions.map(e => e.eventType) : [];
    },
    onQuery(selection, dateRange) {
      this.serverSelection = selection;
      if (dateRange && dateRange.length === 2) {
        this.queryParams.beginDate = dateRange[0];
        this.queryParams.endDate = dateRange[1];
      }
      this.handleQuery();
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList(); },
    getList() {
      this.loading = true;
      const s = this.serverSelection;
      const params = {
        projectId: s.projectId,
        serverIds: (s.serverIds || []).join(','),
        roleSearch: this.queryParams.roleSearch,
        eventTypes: (this.queryParams.eventTypes || []).join(','),
        beginDate: this.queryParams.beginDate,
        endDate: this.queryParams.endDate,
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize
      };
      getEventLog(params).then(r => {
        this.eventLogList = r.rows || [];
        this.total = r.total || 0;
        this.loading = false;
      });
    },
    resetQuery() {
      this.queryParams = { pageNum: 1, pageSize: 10, roleSearch: undefined, eventTypes: [] };
    },

    // ===== 语义解析 =====
    eventName(type) {
      const cfg = this.eventConfigMap[type];
      return cfg ? cfg.eventName : type;
    },
    parseEvent(row) {
      const cfg = this.eventConfigMap[row.event_type];
      if (!cfg || !cfg.paramDefine) return this.rawParams(row);

      const def = cfg.paramDefine;
      const parts = [];
      // n1~n10
      for (let i = 1; i <= 10; i++) {
        const key = 'n' + i;
        const d = def[key];
        const val = row[key];
        if (!d || val == null) continue;
        if (d.type === 'enum' && d.enumValues) {
          parts.push((d.label || key) + ': ' + (d.enumValues[String(val)] || val));
        } else {
          parts.push((d.label || key) + ': ' + val);
        }
      }
      // s1~s5
      for (let i = 1; i <= 5; i++) {
        const key = 's' + i;
        const d = def[key];
        const val = row[key];
        if (!d || !val) continue;
        parts.push((d.label || key) + ': ' + val);
      }
      return parts.length > 0 ? parts.join(', ') : this.rawParams(row);
    },
    rawParams(row) {
      // 无配置时的兜底显示
      let p = [];
      for (let i = 1; i <= 5; i++) { if (row['n'+i] != null) p.push('n'+i+'='+row['n'+i]); }
      for (let i = 1; i <= 2; i++) { if (row['s'+i]) p.push('s'+i+'='+row['s'+i]); }
      return p.join(' ');
    }
  }
};
</script>

<style scoped>
.el-parsed { font-size: 12px; line-height: 1.6; }
</style>
