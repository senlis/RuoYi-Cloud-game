<template>
  <div class="app-container">
    <ServerSelector ref="serverSelector" v-model="serverSelection" @query="onQuery" />
    <div style="margin-top:8px">
      <el-button size="small" icon="el-icon-download" @click="handleExport" :disabled="rows.length===0">导出Excel</el-button>
    </div>

    <div v-if="rows.length > 0" style="margin-top:10px;display:flex;gap:10px;flex-wrap:wrap">
      <el-card style="flex:1;min-width:200px"><div slot="header">注册趋势</div><div ref="chReg" style="height:140px"></div></el-card>
      <el-card style="flex:1;min-width:200px"><div slot="header">登录趋势(DAU)</div><div ref="chDau" style="height:140px"></div></el-card>
      <el-card style="flex:1;min-width:200px"><div slot="header">付费人数趋势</div><div ref="chPay" style="height:140px"></div></el-card>
      <el-card style="flex:1;min-width:200px"><div slot="header">付费金额趋势</div><div ref="chAmt" style="height:140px"></div></el-card>
    </div>

    <el-tabs v-model="viewMode" style="margin-top:12px" type="border-card">
      <!-- Tab1: 按日期 -->
      <el-tab-pane label="按日期" name="date">
        <el-table border :data="dateRows" :summary-method="summary" show-summary
                  v-loading="loading" size="small" class="ov-table">
          <el-table-column type="expand" width="40">
            <template slot-scope="scope">
              <el-table :data="scope.row._servers" size="mini" border class="ov-sub">
                <!-- 占位列：对齐父级 expand(40) + 日期(105) = 145 -->
                <el-table-column width="145" label="服务器">
                  <template slot-scope="s">{{ s.row.server_name || ('服' + s.row.server_id) }}</template>
                </el-table-column>
                <el-table-column label="注册" prop="reg_roles" width="65" />
                <el-table-column label="登录" prop="login_roles" width="65" />
                <el-table-column label="付费" prop="pay_roles" width="60" />
                <el-table-column label="付费次" prop="pay_times" width="68" />
                <el-table-column label="人均付费次" prop="avg_pay_times" width="78" />
                <el-table-column label="付费率" prop="pay_rate" width="60" :formatter="pct" />
                <el-table-column label="付费额" prop="pay_amount" width="78" />
                <el-table-column label="ARPU" prop="pay_arpu" width="62" />
                <el-table-column label="ARPPU" prop="pay_arppu" width="70" />
                <el-table-column label="新登录" prop="new_login" width="62" />
                <el-table-column label="新付费" prop="new_pay_roles" width="62" />
                <el-table-column label="新付费额" prop="new_pay_amount" width="78" />
                <el-table-column label="新付费率" prop="new_pay_rate" width="68" :formatter="pct" />
                <el-table-column label="新登录ARPU" prop="new_login_arpu" width="88" />
                <el-table-column label="新付费ARPU" prop="new_pay_arpu" width="88" />
                <el-table-column label="老登录" prop="old_login" width="62" />
                <el-table-column label="老付费" prop="old_pay_roles" width="62" />
                <el-table-column label="老付费额" prop="old_pay_amount" width="78" />
                <el-table-column label="老付费率" prop="old_pay_rate" width="68" :formatter="pct" />
                <el-table-column label="老登录ARPU" prop="old_login_arpu" width="88" />
                <el-table-column label="老付费ARPU" prop="old_pay_arpu" width="88" />
              </el-table>
            </template>
          </el-table-column>
          <el-table-column label="日期" prop="_date" width="105" fixed />
          <el-table-column label="注册" prop="reg_roles" width="65" />
          <el-table-column label="登录" prop="login_roles" width="65" />
          <el-table-column label="付费" prop="pay_roles" width="60" />
          <el-table-column label="付费次" prop="pay_times" width="68" />
          <el-table-column label="人均付费次" prop="avg_pay_times" width="78" />
          <el-table-column label="付费率" prop="pay_rate" width="60" :formatter="pct" />
          <el-table-column label="付费额" prop="pay_amount" width="78" />
          <el-table-column label="ARPU" prop="pay_arpu" width="62" />
          <el-table-column label="ARPPU" prop="pay_arppu" width="70" />
          <el-table-column label="新登录" prop="new_login" width="62" />
          <el-table-column label="新付费" prop="new_pay_roles" width="62" />
          <el-table-column label="新付费额" prop="new_pay_amount" width="78" />
          <el-table-column label="新付费率" prop="new_pay_rate" width="68" :formatter="pct" />
          <el-table-column label="新登录ARPU" prop="new_login_arpu" width="88" />
          <el-table-column label="新付费ARPU" prop="new_pay_arpu" width="88" />
          <el-table-column label="老登录" prop="old_login" width="62" />
          <el-table-column label="老付费" prop="old_pay_roles" width="62" />
          <el-table-column label="老付费额" prop="old_pay_amount" width="78" />
          <el-table-column label="老付费率" prop="old_pay_rate" width="68" :formatter="pct" />
          <el-table-column label="老登录ARPU" prop="old_login_arpu" width="88" />
          <el-table-column label="老付费ARPU" prop="old_pay_arpu" width="88" />
        </el-table>
      </el-tab-pane>

      <!-- Tab2: 按服务器 -->
      <el-tab-pane label="按服务器" name="server">
        <el-table border :data="rows" :summary-method="summary" show-summary
                  v-loading="loading" size="small" class="ov-table">
          <el-table-column label="服务器" prop="server_id" width="65" fixed />
          <el-table-column label="日期" prop="dt" width="105" fixed />
          <el-table-column label="注册" prop="reg_roles" width="65" />
          <el-table-column label="登录" prop="login_roles" width="65" />
          <el-table-column label="付费" prop="pay_roles" width="60" />
          <el-table-column label="付费次" prop="pay_times" width="68" />
          <el-table-column label="人均付费次" prop="avg_pay_times" width="78" />
          <el-table-column label="付费率" prop="pay_rate" width="60" :formatter="pct" />
          <el-table-column label="付费额" prop="pay_amount" width="78" />
          <el-table-column label="ARPU" prop="pay_arpu" width="62" />
          <el-table-column label="ARPPU" prop="pay_arppu" width="70" />
          <el-table-column label="新登录" prop="new_login" width="62" />
          <el-table-column label="新付费" prop="new_pay_roles" width="62" />
          <el-table-column label="新付费额" prop="new_pay_amount" width="78" />
          <el-table-column label="新付费率" prop="new_pay_rate" width="68" :formatter="pct" />
          <el-table-column label="新登录ARPU" prop="new_login_arpu" width="88" />
          <el-table-column label="新付费ARPU" prop="new_pay_arpu" width="88" />
          <el-table-column label="老登录" prop="old_login" width="62" />
          <el-table-column label="老付费" prop="old_pay_roles" width="62" />
          <el-table-column label="老付费额" prop="old_pay_amount" width="78" />
          <el-table-column label="老付费率" prop="old_pay_rate" width="68" :formatter="pct" />
          <el-table-column label="老登录ARPU" prop="old_login_arpu" width="88" />
          <el-table-column label="老付费ARPU" prop="old_pay_arpu" width="88" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";
import { getOverview } from "@/api/game/analytics";
import { listServer, listServerByRegion } from "@/api/game/server";
import * as echarts from "echarts";

const NUMS = ["reg_roles","login_roles","pay_roles","pay_times","pay_amount","new_login","new_pay_roles","new_pay_amount",
  "avg_pay_times","pay_rate","pay_arpu","pay_arppu","new_pay_rate","new_login_arpu","new_pay_arpu",
  "old_login","old_pay_roles","old_pay_amount","old_pay_rate","old_login_arpu","old_pay_arpu"];

export default {
  name: "AnalyticsOverview",
  components: { ServerSelector },
  data() {
    return { loading: false, rows: [], viewMode: "date", serverSelection: { projectId: null, serverIds: [] }, serverNameMap: {} };
  },
  computed: {
    dateRows() {
      const map = {};
      this.rows.forEach(r => {
        const k = r.dt; if (!k) return;
        if (!map[k]) {
          map[k] = { _date: k, _servers: [] };
          NUMS.forEach(c => map[k][c] = 0);
        }
        const d = map[k];
        d._servers.push(this.ensureNums(r));
        d._servers.sort((a, b) => (a.server_id || 0) - (b.server_id || 0));
      });
      const list = Object.values(map);
      NUMS.forEach(c => list.forEach(d => {
        if (d._servers.length > 0) {
          d[c] = d._servers.reduce((s, sr) => s + (sr[c] || 0), 0);
          if (c === 'pay_rate' || c === 'new_pay_rate' || c === 'old_pay_rate') {
            const loginKey = c === 'pay_rate' ? 'login_roles' : c === 'new_pay_rate' ? 'new_login' : 'old_login';
            const payKey = c === 'pay_rate' ? 'pay_roles' : c === 'new_pay_rate' ? 'new_pay_roles' : 'old_pay_roles';
            const totalLogin = d._servers.reduce((s, sr) => s + (sr[loginKey] || 0), 0);
            d[c] = totalLogin > 0 ? +(d._servers.reduce((s, sr) => s + (sr[payKey] || 0) * 100, 0) / totalLogin).toFixed(2) : 0;
          } else if (/arpu|arppu|avg_pay/.test(c)) {
            // ARPU = 付费/登录, avg_pay_times = pay_times/pay_roles
            d[c] = c === 'avg_pay_times' ? (d.pay_roles > 0 ? +(d.pay_times / d.pay_roles).toFixed(2) : 0)
                  : c === 'pay_arpu' ? (d.login_roles > 0 ? +(d.pay_amount / d.login_roles).toFixed(2) : 0)
                  : c === 'pay_arppu' ? (d.pay_roles > 0 ? +(d.pay_amount / d.pay_roles).toFixed(2) : 0)
                  : c === 'new_login_arpu' ? (d.new_login > 0 ? +(d.new_pay_amount / d.new_login).toFixed(2) : 0)
                  : c === 'new_pay_arpu' ? (d.new_pay_roles > 0 ? +(d.new_pay_amount / d.new_pay_roles).toFixed(2) : 0)
                  : c === 'old_login_arpu' ? (d.old_login > 0 ? +(d.old_pay_amount / d.old_login).toFixed(2) : 0)
                  : c === 'old_pay_arpu' ? (d.old_pay_roles > 0 ? +(d.old_pay_amount / d.old_pay_roles).toFixed(2) : 0) : d[c];
          }
        }
      }));
      list.sort((a, b) => (a._date || '').localeCompare(b._date || ''));
      return list;
    }
  },
  methods: {
    ensureNums(r) {
      const sid = r.server_id;
      const sname = this.serverNameMap[sid] || ('服' + sid);
      const o = { server_id: sid, server_name: sname, dt: r.dt };
      NUMS.forEach(c => {
        let v = r[c] != null ? Number(r[c]) : 0;
        // 比率/ARPU字段保留2位小数
        if (/rate|arpu|arppu|avg_pay/.test(c)) {
          v = Number(v.toFixed(2));
        }
        o[c] = v;
      });
      return o;
    },
    onQuery(sel, daterange) {
      this.serverSelection = sel;
      if (!sel || !sel.projectId || !daterange || daterange.length !== 2) return;
      this.loadData(sel, daterange);
    },
    loadData(sel, daterange) {
      this.loading = true;
      const sids = sel.serverIds || [];
      // 加载服务器名称映射
      if (sids.length > 0) {
        listServer({ pageSize: 500 }).then(r => {
          const map = {};
          (r.rows || []).forEach(s => { map[s.serverId] = s.serverName; });
          this.serverNameMap = map;
        }).catch(() => {});
      }
      getOverview({ projectId: sel.projectId, serverIds: sids.join(','), beginDate: daterange[0], endDate: daterange[1] }).then(r => {
        this.rows = (r.data || []).map(row => this.ensureNums(row));
        this.rows.sort((a, b) => (a.dt||'').localeCompare(b.dt||'') || (a.server_id - b.server_id));
        this.loading = false;
        this.$nextTick(() => this.renderCharts());
      }).catch(() => { this.loading = false; this.rows = []; });
    },
    pct(r, c, v) { return v != null && v !== '' ? Number(v).toFixed(2) + '%' : '0.00%'; },
    summary({ columns, data }) {
      const sums = [];
      columns.forEach((col, idx) => {
        if (idx === 0) { sums[idx] = '合计'; return; }
        const p = col.property;
        if (!p || p === '_date') { sums[idx] = ''; return; }
        const vals = data.map(r => Number(r[p]) || 0);
        const total = vals.reduce((a, b) => a + b, 0);
        if (/rate|arpu|arppu|avg/.test(p)) {
          const w = data.map(r => Number(r.login_roles) || 1).reduce((a, b) => a + b, 0);
          sums[idx] = w > 0 ? Number((vals.reduce((a, v, i) => a + v * (Number(data[i].login_roles)||1), 0) / w).toFixed(2)) : 0;
        } else {
          sums[idx] = total;
        }
      });
      return sums;
    },
    renderCharts() {
      if (this.rows.length === 0) return;
      const dates = [...new Set(this.rows.map(r => r.dt).filter(Boolean))].sort();
      if (dates.length === 0) return;
      const seriesData = ['chReg','chDau','chPay','chAmt'].map(ref => {
        const el = this.$refs[ref]; if (!el) return;
        return {
          el, key: ref === 'chReg' ? 'reg_roles' : ref === 'chDau' ? 'login_roles' : ref === 'chPay' ? 'pay_roles' : 'pay_amount',
          name: ref === 'chReg' ? '注册' : ref === 'chDau' ? 'DAU' : ref === 'chPay' ? '付费人数' : '付费金额'
        };
      }).filter(Boolean);
      seriesData.forEach(({ el, key, name }) => {
        const data = dates.map(d => this.rows.filter(r => r.dt === d).reduce((s, r) => s + (Number(r[key]) || 0), 0));
        const chart = echarts.init(el);
        chart.setOption({
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: dates, axisLabel: { show: false } },
          yAxis: { type: 'value', name, nameTextStyle: { fontSize: 10 } },
          series: [{ data, type: 'line', smooth: true, areaStyle: { opacity: 0.15 }, lineStyle: { width: 2 } }],
          grid: { left: 50, right: 10, top: 10, bottom: 20 }
        });
        window.addEventListener('resize', () => chart.resize());
      });
    },
    handleExport() { this.$message.info("导出功能待实现"); }
  }
};
</script>
<style scoped>
.ov-table .el-table__header th { font-size: 11px; padding: 4px 2px; white-space: nowrap; }
.ov-table .el-table__body td { font-size: 12px; }
.ov-sub .el-table__header th { font-size: 11px; padding: 4px 2px; white-space: nowrap; }
.ov-sub { margin-left: 0; }
</style>
