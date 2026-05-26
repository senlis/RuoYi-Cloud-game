<template>
  <div class="ss-root">
    <!-- ====== 项目 ====== -->
    <div class="ss-row">
      <div class="ss-row-label">项目</div>
      <div class="ss-row-body" :class="{ disabled: true }">
        <el-checkbox v-model="projectAll" :indeterminate="projectIndeterminate"
                     size="small" border @change="toggleProjectAll">全选项目</el-checkbox>
        <el-checkbox v-for="p in projects" :key="p.projectId"
                     :label="p.projectId" v-model="selectedProjects"
                     size="small" border @change="onProjectChange">{{ p.projectName }}</el-checkbox>
      </div>
    </div>

    <!-- ====== 渠道（依赖项目） ====== -->
    <div class="ss-row" :class="{ 'ss-disabled': selectedProjects.length === 0 }">
      <div class="ss-row-label">渠道</div>
      <div class="ss-row-body">
        <el-checkbox v-if="channels.length > 0" v-model="channelAll" :indeterminate="channelIndeterminate"
                     size="small" border @change="toggleChannelAll">全选渠道</el-checkbox>
        <span v-if="selectedProjects.length === 0 && channels.length === 0" class="ss-hint">请先选择项目</span>
        <el-checkbox v-for="c in channels" :key="c.channelId"
                     :label="c.channelId" v-model="selectedChannels"
                     size="small" border @change="onChannelChange">{{ c.channelName }}</el-checkbox>
      </div>
    </div>

    <!-- ====== 分区（依赖渠道） ====== -->
    <div class="ss-row" :class="{ 'ss-disabled': selectedChannels.length === 0 }">
      <div class="ss-row-label">分区</div>
      <div class="ss-row-body">
        <el-checkbox v-if="regions.length > 0" v-model="regionAll" :indeterminate="regionIndeterminate"
                     size="small" border @change="toggleRegionAll">全选分区</el-checkbox>
        <span v-if="selectedChannels.length === 0 && regions.length === 0" class="ss-hint">请先选择渠道</span>
        <el-checkbox v-for="r in regions" :key="r.regionId"
                     :label="r.regionId" v-model="selectedRegions"
                     size="small" border @change="onRegionChange">{{ r.regionName }}</el-checkbox>
      </div>
    </div>

    <!-- ====== 服务器 ====== -->
    <div class="ss-row" :class="{ 'ss-disabled': selectedRegions.length === 0 }">
      <div class="ss-row-label">服务器</div>
      <div class="ss-row-body">
        <template v-if="selectedRegions.length > 0">
          <div class="ss-toolbar">
            <el-checkbox v-model="serverAll" :indeterminate="serverIndeterminate"
                         size="small" border @change="toggleServerAll">全选</el-checkbox>
            <el-checkbox size="small" border @change="toggleServerReverse">反选</el-checkbox>
            <el-input v-model="serverSearch" placeholder="搜索服务器名" size="small" clearable
                      prefix-icon="el-icon-search" style="width:160px" />
            <span style="font-size:12px;color:#909399">开服天数</span>
            <el-input-number v-model="daysFrom" size="small" :min="0" controls-position="right"
                             style="width:100px" placeholder="起始" />
            <span style="color:#C0C4CC">~</span>
            <el-input-number v-model="daysTo" size="small" :min="0" controls-position="right"
                             style="width:100px" placeholder="截止" />
            <el-button size="small" @click="applyDaysFilter">筛选</el-button>
            <span class="ss-stats">({{ filteredServers.length }}服/已选{{ selectedServers.length }})</span>
          </div>
          <div class="ss-server-container">
            <div class="ss-server-item" v-for="s in filteredServers" :key="'svr'+s.serverId+'_'+s.regionId">
              <el-checkbox :label="''+s.serverId+'_'+s.regionId" v-model="selectedServers"
                           size="small" @change="emitChange">
                <span class="ss-name">{{ s.serverName }}</span>
                <span v-if="s.openTime" class="ss-days">(开服第{{ dayz(s) }}天)</span>
                <span v-else class="ss-days">(未开服)</span>
              </el-checkbox>
            </div>
            <div v-if="filteredServers.length === 0 && servers.length > 0" class="ss-empty">暂无匹配服务器</div>
            <div v-if="servers.length === 0" class="ss-empty">加载中…</div>
          </div>
        </template>
        <span v-else class="ss-hint">请先选择分区</span>
      </div>
    </div>

    <!-- ====== 操作区 ====== -->
    <div class="ss-actions">
      <el-date-picker v-if="showDatePicker" v-model="dateRange" type="daterange" size="small"
                      range-separator="~" start-placeholder="开始日期" end-placeholder="结束日期"
                      value-format="yyyy-MM-dd" style="margin-right:10px" />
      <el-button type="primary" size="small" icon="el-icon-search" @click="doQuery">查询</el-button>
      <el-button size="small" icon="el-icon-refresh" @click="resetAll">重置</el-button>
    </div>
  </div>
</template>

<script>
import { listProject } from "@/api/game/project";
import { listChannelByProject } from "@/api/game/channel";
import { listRegionByChannel } from "@/api/game/region";
import { listServerByRegion } from "@/api/game/server";

export default {
  name: "ServerSelector",
  props: {
    value: { type: Object, default: () => ({ projectId: null, serverIds: [] }) },
    showDatePicker: { type: Boolean, default: true }
  },
  data() {
    return {
      projects: [], channels: [], regions: [], servers: [],
      selectedProjects: [], selectedChannels: [], selectedRegions: [], selectedServers: [],
      projectAll: false, projectIndeterminate: false,
      channelAll: false, channelIndeterminate: false,
      regionAll: false, regionIndeterminate: false,
      serverAll: false, serverIndeterminate: false,
      serverSearch: "", daysFrom: null, daysTo: null, dateRange: [],
      _serverDays: {},
      _restoring: false,
      _pendingServerIds: null
    };
  },
  computed: {
    filteredServers() {
      let list = this.servers;
      const q = (this.serverSearch || "").toLowerCase();
      if (q) list = list.filter(s => (s.serverName || "").toLowerCase().includes(q));
      if (this.daysFrom || this.daysTo) {
        const f = this.daysFrom || 0;
        const t = this.daysTo || 99999;
        list = list.filter(s => { const d = this.serverDays(s); return d >= f && d <= t; });
      }
      return list;
    }
  },
  watch: {
    value: {
      deep: true,
      handler(val) {
        // 重置：外部清空选择（如新建邮件时）— 即使 _restoring 中也要强制清空
        if (!val || !val.projectId) {
          this._restoring = false
          this._pendingServerIds = null
          this.selectedProjects = []; this.selectedChannels = []; this.selectedRegions = []; this.selectedServers = [];
          this.channels = []; this.regions = []; this.servers = [];
          return
        }
        if (this._restoring) return
        const currPid = this.selectedProjects.length > 0 ? this.selectedProjects[0] : null
        if (currPid === val.projectId && !val._forceRestore) return
        this._restoring = true
        this._pendingServerIds = val.serverIds || []
        // 选中项目
        this.selectedProjects = [val.projectId]
        // 加载渠道数据
        this.channels = []
        this.loadChannels([val.projectId])
      }
    },
    selectedProjects(v) { this.projectAll = v.length > 0 && v.length === this.projects.length;
      this.projectIndeterminate = v.length > 0 && v.length < this.projects.length; },
    channels(v) {
      // 回填：渠道数据已加载 → 选中渠道（有记录则用记录，无记录则全选）→ 加载分区
      if (this._restoring && v.length > 0 && this.regions.length === 0) {
        const want = (this.value && this.value.channelIds) || []
        if (want.length > 0) { this.selectedChannels = want; this.loadRegions(want) }
        else { this.selectedChannels = v.map(c => c.channelId); this.loadRegions(this.selectedChannels) }
      }
    },
    regions(v) {
      // 回填：分区数据已加载 → 选中分区（有记录则用记录，无记录则全选）→ 加载服务器
      if (this._restoring && v.length > 0 && this.servers.length === 0) {
        const want = (this.value && this.value.regionIds) || []
        if (want.length > 0) { this.selectedRegions = want; this.loadServers(want) }
        else { this.selectedRegions = v.map(r => r.regionId); this.loadServers(this.selectedRegions) }
      }
    },
    selectedChannels(v) { this.channelAll = v.length > 0 && v.length === this.channels.length;
      this.channelIndeterminate = v.length > 0 && v.length < this.channels.length; },
    selectedRegions(v) { this.regionAll = v.length > 0 && v.length === this.regions.length;
      this.regionIndeterminate = v.length > 0 && v.length < this.regions.length; },
    selectedServers(v) { this.serverAll = v.length > 0 && v.length === this.servers.length;
      this.serverIndeterminate = v.length > 0 && v.length < this.servers.length; }
  },
  created() { this.loadProjects(); },
  methods: {
    loadProjects() {
      listProject({ pageSize: 200 }).then(r => { this.projects = r.rows || []; });
    },
    loadChannels(projectIds) {
      if (!projectIds || projectIds.length === 0) { this.channels = []; this.selectedChannels = []; return; }
      Promise.all(projectIds.map(pid => listChannelByProject(pid))).then(results => {
        const map = new Map();
        results.forEach(r => { if (r && r.data) r.data.forEach(c => map.set(c.channelId, c)); });
        this.channels = [...map.values()];
      });
    },
    loadRegions(channelIds) {
      if (!channelIds || channelIds.length === 0) { this.regions = []; this.selectedRegions = []; return; }
      Promise.all(channelIds.map(cid => listRegionByChannel(cid))).then(results => {
        const map = new Map();
        results.forEach(r => { if (r && r.data) r.data.forEach(reg => map.set(reg.regionId, reg)); });
        this.regions = [...map.values()];
      });
    },
    loadServers(regionIds) {
      if (!regionIds || regionIds.length === 0) { this.servers = []; this.selectedServers = []; return; }
      Promise.all(regionIds.map(rid => listServerByRegion(rid).catch(e => { console.error('server load err', rid, e); return { data: [] }; })))
        .then(results => {
          const map = new Map();
          results.forEach(r => {
            const list = r && r.data ? r.data : (Array.isArray(r) ? r : []);
            list.forEach(s => {
              if (s && s.serverId != null) {
                const k = s.serverId + '_' + s.regionId;
                if (!map.has(k)) map.set(k, s);
              }
            });
          });
          const arr = [...map.values()].sort((a, b) => (a.serverId || 0) - (b.serverId || 0));
          this.servers = arr;
          // 外部回填：恢复服务器选中状态
          if (this._restoring && this._pendingServerIds) {
            this.selectedServers = arr
              .filter(s => this._pendingServerIds.includes(s.serverId))
              .map(s => s.serverId + '_' + s.regionId);
            this._restoring = false;
            this._pendingServerIds = null;
            this.emitChange();
          } else {
            this.selectedServers = [];
          }
          this._serverDays = {};
          this.serverSearch = "";
        }).catch(e => {
          console.error('loadServers failed', e);
          this.servers = [];
        });
    },
    onProjectChange() {
      this.selectedChannels = []; this.selectedRegions = []; this.selectedServers = [];
      this.regions = []; this.servers = [];
      this.loadChannels(this.selectedProjects);
    },
    onChannelChange() {
      this.selectedRegions = []; this.selectedServers = [];
      this.regions = []; this.servers = [];
      if (this.selectedChannels.length > 0) this.loadRegions(this.selectedChannels);
    },
    onRegionChange() {
      this.selectedServers = [];
      this.servers = [];
      if (this.selectedRegions.length > 0) this.loadServers(this.selectedRegions);
    },
    toggleProjectAll(v) { this.selectedProjects = v ? this.projects.map(p => p.projectId) : []; this.onProjectChange(); },
    toggleChannelAll(v) { this.selectedChannels = v ? this.channels.map(c => c.channelId) : []; this.onChannelChange(); },
    toggleRegionAll(v) { this.selectedRegions = v ? this.regions.map(r => r.regionId) : []; this.onRegionChange(); },
    toggleServerAll(v) { this.selectedServers = v ? this.filteredServers.map(s => s.serverId + '_' + s.regionId) : []; this.emitChange(); },
    toggleServerReverse() {
      const all = this.filteredServers.map(s => s.serverId + '_' + s.regionId);
      this.selectedServers = all.filter(v => !this.selectedServers.includes(v));
      this.emitChange();
    },
    applyDaysFilter() {},
    emitChange() {
      const ids = this.selectedServers.map(v => parseInt(v.split('_')[0]));
      const pid = this.selectedProjects.length > 0 ? this.selectedProjects[0] : null;
      this.$emit("input", {
        projectId: pid,
        channelIds: [...this.selectedChannels],
        regionIds: [...this.selectedRegions],
        serverIds: ids
      });
    },
    doQuery() {
      if (this.selectedProjects.length === 0) { this.$message.warning("请先选择项目"); return; }
      if (this.selectedChannels.length === 0) { this.$message.warning("请先选择渠道"); return; }
      if (this.selectedRegions.length === 0) { this.$message.warning("请先选择分区"); return; }
      if (this.showDatePicker && (!this.dateRange || this.dateRange.length !== 2)) { this.$message.warning("请选择日期范围"); return; }
      this.$emit("query", {
        projectId: this.selectedProjects.length > 0 ? this.selectedProjects[0] : null,
        serverIds: this.selectedServers.map(v => parseInt(v.split('_')[0]))
      }, this.dateRange);
    },
    resetAll() {
      this.selectedProjects = []; this.selectedChannels = []; this.selectedRegions = []; this.selectedServers = [];
      this.channels = []; this.regions = []; this.servers = [];
      this.serverSearch = ""; this.daysFrom = null; this.daysTo = null; this.dateRange = [];
      this._serverDays = {};
      this.$emit("input", { projectId: null, channelIds: [], regionIds: [], serverIds: [] });
    },
    dayz(s) {
      if (!s || !s.openTime) return 0;
      try {
        let v = s.openTime;
        if (typeof v === 'number') return Math.max(1, Math.floor((Date.now() - v) / 86400000));
        if (typeof v === 'string') return Math.max(1, Math.floor((Date.now() - new Date(v).getTime()) / 86400000));
      } catch(e) {}
      return 0;
    },
    serverDays(s) {
      if (!s || !s.openTime) return 0;
      const k = s.serverId + '_' + s.regionId;
      if (!(k in this._serverDays)) {
        try {
          this._serverDays[k] = Math.max(1, Math.floor((Date.now() - new Date(s.openTime).getTime()) / 86400000));
        } catch(e) { this._serverDays[k] = 0; }
      }
      return this._serverDays[k] || 0;
    }
  }
};
</script>

<style scoped>
.ss-root { border: 1px solid #e4e7ed; border-radius: 4px; background: #fff; }
.ss-row { display: flex; border-bottom: 1px solid #ebeef5; padding: 8px 12px; align-items: flex-start; min-height: 36px; }
.ss-row:last-of-type, .ss-row-noborder { border-bottom: none; }
.ss-disabled { opacity: 0.5; pointer-events: none; }
.ss-row-label { width: 64px; flex-shrink: 0; font-size: 13px; font-weight: 600; color: #303133; line-height: 28px; }
.ss-row-body { flex: 1; display: flex; flex-wrap: wrap; gap: 6px; align-items: center; min-height: 28px; }
.ss-hint { color: #c0c4cc; font-size: 12px; line-height: 28px; }
.ss-toolbar { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; width: 100%; margin-bottom: 6px; }
.ss-stats { font-size: 11px; color: #909399; margin-left: 4px; white-space: nowrap; }
.ss-server-container { max-height: 260px; overflow-y: auto; width: 100%; display: flex; flex-flow: row wrap; gap: 2px 16px; align-content: flex-start; }
.ss-server-container::-webkit-scrollbar { width: 6px; }
.ss-server-container::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 3px; }
.ss-server-item { line-height: 30px; min-width: 200px; }
.ss-name { font-size: 13px; }
.ss-days { font-size: 11px; color: #909399; margin-left: 2px; }
.ss-actions { display: flex; align-items: center; padding: 8px 12px; border-top: 1px solid #ebeef5; background: #fafafa; border-radius: 0 0 4px 4px; }
</style>
