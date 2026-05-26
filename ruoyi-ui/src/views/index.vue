<template>
  <div class="app-container">
    <!-- 顶部：欢迎与时间 -->
    <div class="welcome-header">
      <div class="welcome-left">
        <h2 class="welcome-title">游戏运营管理平台</h2>
        <p class="welcome-subtitle">{{ currentDate }} {{ currentWeekday }}，欢迎回来</p>
      </div>
      <div class="welcome-right">
        <el-tag type="success" effect="dark" size="small">系统运行正常</el-tag>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card" style="border-left: 4px solid #409eff;">
          <div class="stat-icon" style="background:#ecf5ff"><i class="el-icon-s-check" style="color:#409eff"></i></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.pendingAudit }}</div>
            <div class="stat-label">待审核邮件</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" style="border-left: 4px solid #67c23a;">
          <div class="stat-icon" style="background:#f0f9eb"><i class="el-icon-s-grid" style="color:#67c23a"></i></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.projectCount }}</div>
            <div class="stat-label">管理项目数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" style="border-left: 4px solid #e6a23c;">
          <div class="stat-icon" style="background:#fdf6ec"><i class="el-icon-user" style="color:#e6a23c"></i></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.todayActive }}</div>
            <div class="stat-label">今日活跃</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card" style="border-left: 4px solid #f56c6c;">
          <div class="stat-icon" style="background:#fef0f0"><i class="el-icon-coin" style="color:#f56c6c"></i></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.todayRecharge }}</div>
            <div class="stat-label">今日充值</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 待办事项 -->
      <el-col :span="8">
        <el-card shadow="never" class="section-card">
          <div slot="header" class="section-header">
            <span><i class="el-icon-bell" style="color:#e6a23c;margin-right:6px"></i>待办事项</span>
          </div>
          <div class="todo-list">
            <div class="todo-item" v-if="stats.pendingAudit > 0">
              <el-tag type="warning" size="mini" class="todo-badge">{{ stats.pendingAudit }}</el-tag>
              <span class="todo-text">待审核的 GM 邮件</span>
              <el-button size="mini" type="text" @click="$router.push('/gm/mail/audit')">去审核</el-button>
            </div>
            <div class="todo-item" v-else>
              <el-tag type="success" size="mini" class="todo-badge">0</el-tag>
              <span class="todo-text" style="color:#909399">暂无待办</span>
            </div>
          </div>
        </el-card>

        <!-- 系统状态 -->
        <el-card shadow="never" class="section-card" style="margin-top:16px">
          <div slot="header" class="section-header">
            <span><i class="el-icon-monitor" style="color:#409eff;margin-right:6px"></i>服务状态</span>
          </div>
          <div class="service-list">
            <div class="service-item"><span class="service-name">Gateway</span><el-tag size="mini" type="success">运行中</el-tag></div>
            <div class="service-item"><span class="service-name">System</span><el-tag size="mini" type="success">运行中</el-tag></div>
            <div class="service-item"><span class="service-name">Bridge</span><el-tag size="mini" type="success">运行中</el-tag></div>
            <div class="service-item"><span class="service-name">Auth</span><el-tag size="mini" type="success">运行中</el-tag></div>
          </div>
        </el-card>
      </el-col>

      <!-- 快捷入口 -->
      <el-col :span="16">
        <el-card shadow="never" class="section-card">
          <div slot="header" class="section-header">
            <span><i class="el-icon-s-operation" style="color:#409eff;margin-right:6px"></i>快捷入口</span>
          </div>
          <el-row :gutter="12">
            <el-col :xs="12" :sm="8" v-for="item in quickLinks" :key="item.path">
              <div class="quick-link" @click="$router.push(item.path)">
                <div class="ql-icon" :style="{background: item.bg}"><i :class="item.icon" :style="{color: item.color}"></i></div>
                <div class="ql-label">{{ item.label }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 最近操作 -->
        <el-card shadow="never" class="section-card" style="margin-top:16px">
          <div slot="header" class="section-header">
            <span><i class="el-icon-time" style="color:#909399;margin-right:6px"></i>最近创建的 GM 邮件</span>
          </div>
          <el-table :data="recentMails" size="small" empty-text="暂无数据">
            <el-table-column prop="title" label="邮件标题" min-width="140" :show-overflow-tooltip="true" />
            <el-table-column label="状态" width="90">
              <template slot-scope="s">{{ statusLabels[s.row.status] || '未知' }}</template>
            </el-table-column>
            <el-table-column label="时间" width="150">
              <template slot-scope="s">{{ s.row.time ? parseTime(s.row.time) : '-' }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDashboardStats } from "@/api/game/dashboard"

export default {
  name: "Dashboard",
  data() {
    return {
      stats: {
        pendingAudit: 0,
        failedPush: 0,
        projectCount: 0,
        todayActive: "-",
        todayNew: "-",
        todayRecharge: "-"
      },
      recentMails: [],
      statusLabels: { 0: '待审核', 1: '审核通过', 2: '已发送', 3: '发送失败', 4: '驳回', 5: '已撤回' },
      quickLinks: [
        { label: 'GM 邮件', path: '/gm/mail', icon: 'el-icon-message', color: '#409eff', bg: '#ecf5ff' },
        { label: '邮件审核', path: '/gm/mail/audit', icon: 'el-icon-s-promotion', color: '#67c23a', bg: '#f0f9eb' },
        { label: '道具配置', path: '/game/item', icon: 'el-icon-goods', color: '#e6a23c', bg: '#fdf6ec' },
        { label: '角色查询', path: '/bridge/role', icon: 'el-icon-user', color: '#409eff', bg: '#ecf5ff' },
        { label: '订单查询', path: '/bridge/payOrder', icon: 'el-icon-coin', color: '#f56c6c', bg: '#fef0f0' },
        { label: '用户查询', path: '/bridge/user', icon: 'el-icon-s-custom', color: '#67c23a', bg: '#f0f9eb' },
        { label: '项目管理', path: '/game/project', icon: 'el-icon-s-home', color: '#909399', bg: '#f5f7fa' },
        { label: '渠道配置', path: '/bridge/channelArg', icon: 'el-icon-setting', color: '#e6a23c', bg: '#fdf6ec' },
        { label: '平台配置', path: '/bridge/platform', icon: 'el-icon-connection', color: '#409eff', bg: '#ecf5ff' },
      ]
    }
  },
  computed: {
    currentDate() {
      const d = new Date()
      return d.getFullYear() + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日'
    },
    currentWeekday() {
      return ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][new Date().getDay()]
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      getDashboardStats().then(res => {
        const d = res.data || res
        if (d.pendingAudit !== undefined) this.stats.pendingAudit = d.pendingAudit
        if (d.projectCount !== undefined) this.stats.projectCount = d.projectCount
        if (d.todayActive !== undefined) this.stats.todayActive = d.todayActive
        if (d.todayRecharge !== undefined) this.stats.todayRecharge = d.todayRecharge
        if (d.recentMails) this.recentMails = d.recentMails
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.welcome-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.welcome-title { margin: 0; font-size: 22px; color: #303133; }
.welcome-subtitle { margin: 4px 0 0; font-size: 13px; color: #909399; }
.stats-row { margin-bottom: 16px !important; }
.stat-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 12px;
}
.stat-icon i { font-size: 24px; }
.stat-body { flex: 1; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 2px; }
.section-card { border-radius: 6px; }
.section-header { font-size: 14px; font-weight: 600; color: #303133; padding: 2px 0; }
.todo-list { min-height: 60px; }
.todo-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; }
.todo-badge { flex-shrink: 0; }
.todo-text { flex: 1; font-size: 13px; color: #606266; }
.service-list { display: flex; flex-wrap: wrap; gap: 8px; }
.service-item {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 10px; background: #fafafa; border-radius: 4px;
}
.service-name { font-size: 12px; color: #606266; }
.quick-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background .2s;
  margin-bottom: 8px;
}
.quick-link:hover { background: #f5f7fa; }
.ql-icon {
  width: 44px; height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
}
.ql-icon i { font-size: 22px; }
.ql-label { font-size: 12px; color: #606266; text-align: center; }
</style>
