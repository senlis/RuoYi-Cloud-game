<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="邮件标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入邮件标题" clearable style="width: 240px"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="邮件状态" clearable style="width: 240px">
          <el-option label="待审核" :value="0" />
          <el-option label="审核通过" :value="1" />
          <el-option label="已发送" :value="2" />
          <el-option label="发送失败" :value="3" />
          <el-option label="驳回" :value="4" />
          <el-option label="已撤回" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss"
          type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['gm:mail:add']">新建邮件</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple"
          @click="handleDelete" v-hasPermi="['gm:mail:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 邮件列表 -->
    <el-table v-loading="loading" :data="mailList" @selection-change="handleSelectionChange"
      :row-class-name="tableRowClassName">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="mailId" width="70" />
      <el-table-column label="邮件标题" align="center" prop="title" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="项目" align="center" width="120">
        <template slot-scope="scope">{{ scope.row.projectName || 'ID:' + scope.row.projectId }}</template>
      </el-table-column>
      <el-table-column label="附件" align="center" width="180" :show-overflow-tooltip="true">
        <template slot-scope="scope">{{ rewardsSummary(scope.row.rewards) }}</template>
      </el-table-column>
      <el-table-column label="创建人" align="center" prop="createdBy" width="120" />
      <el-table-column label="目标范围" align="center" width="100">
        <template slot-scope="scope">{{ targetTypeLabel(scope.row.targetType) }}</template>
      </el-table-column>
      <el-table-column label="发送方式" align="center" width="80">
        <template slot-scope="scope">{{ scope.row.sendType === 0 ? '立即' : '定时' }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          <el-tag v-if="scope.row.failedServerIds" type="danger" size="mini" style="margin-left:4px">部分失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发送时间" align="center" prop="sendTime" width="160">
        <template slot-scope="scope">
          <span v-if="scope.row.sendTime">{{ parseTime(scope.row.sendTime) }}</span>
          <span v-else style="color:#c0c4cc">--</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" min-width="240">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)"
            v-if="scope.row.status === 0 || scope.row.status === 4" v-hasPermi="['gm:mail:edit']">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-if="scope.row.status === 0 || scope.row.status === 4" v-hasPermi="['gm:mail:remove']">删除</el-button>
          <el-button size="mini" type="text" icon="el-icon-s-promotion" @click="handleSubmit(scope.row)"
            v-if="scope.row.status === 0" v-hasPermi="['gm:mail:edit']">提交审核</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh" @click="handleRetry(scope.row)"
            v-if="scope.row.failedServerIds" v-hasPermi="['gm:mail:edit']" style="color:#e6a23c">重推</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 查看详情对话框 -->
    <el-dialog title="邮件详情" :visible.sync="detailOpen" width="820px" append-to-body top="5vh">
      <div class="detail-container">

        <!-- 基本信息 -->
        <div class="detail-card">
          <div class="detail-card-title">基本信息</div>
          <table class="detail-table">
            <tr><td class="dt-label">邮件标题</td><td class="dt-value" colspan="3">{{ detail.title }}</td></tr>
            <tr>
              <td class="dt-label">发送方式</td><td class="dt-value">{{ detail.sendType === 0 ? '立即发送' : '定时发送' }}</td>
              <td class="dt-label" style="width:90px">有效期</td><td class="dt-value">{{ detail.expireDays }} 天</td>
            </tr>
            <tr>
              <td class="dt-label">发送时间</td><td class="dt-value">{{ detail.sendTime ? parseTime(detail.sendTime) : '立即发送' }}</td>
              <td class="dt-label">创建人</td><td class="dt-value">{{ detail.createdBy || '-' }}</td>
            </tr>
          </table>
        </div>

        <!-- 目标范围 -->
        <div class="detail-card">
          <div class="detail-card-title">目标范围</div>
          <table class="detail-table">
            <tr>
              <td class="dt-label">目标</td>
              <td class="dt-value" colspan="3">{{ targetTypeLabel(detail.targetType) }}</td>
            </tr>
            <tr v-if="detail.serverIds">
              <td class="dt-label">服务器</td>
              <td class="dt-value" colspan="3">
                <div>项目：<b>{{ detailProjectName || '加载中…' }}</b> <span style="color:#909399;font-size:12px">(ID: {{ detail.projectId }})</span></div>
                <div style="margin-top:2px;display:flex;flex-wrap:wrap;gap:4px">
                  <el-tag v-for="s in detailServerNames" :key="s"
                    :type="detail.failedServerIds && detail.failedServerIds.split(',').includes(s.split('(').pop().replace(')','')) ? 'danger' : 'success'"
                    size="small" style="margin:1px">{{ s }}</el-tag>
                  <span v-if="detailServerNames.length === 0" style="color:#909399">加载中…</span>
                </div>
                <div style="color:#909399;font-size:12px;margin-top:4px">共 {{ detail.serverIds ? detail.serverIds.split(',').length : 0 }} 服</div>
              </td>
            </tr>
            <tr v-if="detail.targetType === 1">
              <td class="dt-label">筛选条件</td>
              <td class="dt-value" colspan="3">
                等级 Lv.{{ detail.minLevel || 1 }} ~ Lv.{{ detail.maxLevel || 999 }}，
                VIP {{ detail.minVip || 0 }} ~ {{ detail.maxVip || 99 }}
              </td>
            </tr>
            <tr v-if="detail.targetType === 2">
              <td class="dt-label">指定玩家</td>
              <td class="dt-value" colspan="3">
                <pre style="margin:0;font-size:12px;background:#f5f7fa;padding:8px;border-radius:4px;max-height:120px;overflow-y:auto">{{ detail.targetPlayers }}</pre>
              </td>
            </tr>
          </table>
        </div>

        <!-- 邮件内容 -->
        <div class="detail-card">
          <div class="detail-card-title">邮件内容</div>
          <div style="border:1px solid #e4e7ed;border-radius:4px;padding:12px;min-height:80px;background:#fff;line-height:1.7;font-size:14px"
            v-html="detail.content" class="gm-mail-preview"></div>
        </div>

        <!-- 附件奖励 -->
        <div class="detail-card" v-if="detailRewards.length > 0">
          <div class="detail-card-title">附件奖励（{{ detailRewards.length }} 件）</div>
          <el-table :data="detailRewards" size="small" style="width:100%">
            <el-table-column label="道具ID" prop="itemId" width="120" />
            <el-table-column label="道具名称" prop="itemName" />
            <el-table-column label="数量" prop="count" width="100" />
          </el-table>
        </div>

        <!-- 推送失败 -->
        <div class="detail-card" v-if="detail.failedServerIds">
          <div class="detail-card-title">推送信息</div>
          <el-tag type="danger" style="margin-top:4px">部分服务器推送失败</el-tag>
          <span style="margin-left:8px;color:#e6a23c;font-size:13px">{{ detail.failedServerIds }}</span>
        </div>
        <!-- 驳回原因 -->
        <div class="detail-card" v-if="detail.auditRemark">
          <div class="detail-card-title">审核信息</div>
          <el-tag type="danger" style="margin-top:4px">{{ detail.auditRemark }}</el-tag>
        </div>

      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listGmMail, getGmMail, delGmMail, submitGmMail, retryGmMail } from "@/api/gm/mail"
import { getProject } from "@/api/game/project"
import { listServer } from "@/api/game/server"

export default {
  name: "GmMail",
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      mailList: [],
      ids: [],
      single: true,
      multiple: true,
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        status: undefined
      },
      // 查看详情
      detailOpen: false,
      detail: {},
      detailRewards: [],
      detailProjectName: "",
      detailServerNames: []
    }
  },
  created() {
    this.getList()
  },
  activated() {
    this.getList()
  },
  methods: {
    /** 解析奖励内容：道具名x数量 */
    rewardsSummary(rewards) {
      if (!rewards) return ''
      try {
        const list = JSON.parse(rewards)
        return list.map(r => r.itemName + (r.count ? 'x' + r.count : '')).join('、')
      } catch(e) { return '解析失败' }
    },
    getList() {
      this.loading = true
      const params = this.addDateRange({...this.queryParams}, this.dateRange)
      listGmMail(params).then(response => {
        this.mailList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.mailId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // ---- 导航 ----
    handleAdd() {
      this.$router.push('/gm/mail/add')
    },
    handleEdit(row) {
      this.$router.push('/gm/mail/edit/' + row.mailId)
    },
    handleView(row) {
      getGmMail(row.mailId).then(response => {
        this.detail = response.data
        this.detailProjectName = ""
        this.detailServerNames = []
        try { this.detailRewards = JSON.parse(response.data.rewards || '[]') } catch(e) { this.detailRewards = [] }
        const data = response.data
        // 加载项目名
        if (data.projectId) {
          getProject(data.projectId).then(r => { this.detailProjectName = r.data ? r.data.projectName : '' }).catch(() => {})
        }
        // 加载服务器名
        if (data.serverIds) {
          const ids = data.serverIds.split(',').map(Number)
          listServer({ pageSize: 999 }).then(r => {
            const allServers = r.rows || []
            this.detailServerNames = ids.map(id => {
              const s = allServers.find(sv => sv.serverId === id)
              return s ? s.serverName + '(' + id + ')' : '#' + id
            })
          }).catch(() => {})
        }
        this.detailOpen = true
      })
    },
    /** 重推失败服务器 */
    handleRetry(row) {
      this.$modal.confirm('确定重推失败服务器？').then(() => {
        return retryGmMail(row.mailId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("重推完成")
      }).catch(() => {})
    },
    /** 行样式：失败高亮 */
    tableRowClassName({ row }) {
      if (row.failedServerIds) return 'row-failed'
      return ''
    },
    handleSubmit(row) {
      this.$modal.confirm('确定提交该邮件进行审核？').then(() => {
        return submitGmMail(row.mailId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("已提交审核")
      }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row ? [row.mailId] : this.ids
      this.$modal.confirm('确定删除选中邮件？仅待审核或驳回状态可删除。').then(() => {
        const promises = ids.map(id => delGmMail(id))
        return Promise.all(promises)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    // ---- 格式化 ----
    targetTypeLabel(val) {
      const map = { 0: '全服', 1: '条件筛选', 2: '指定玩家' }
      return map[val] || '未知'
    },
    statusTag(val) {
      const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'info', 5: 'info' }
      return map[val] || 'info'
    },
    statusLabel(val) {
      const map = { 0: '待审核', 1: '审核通过', 2: '已发送', 3: '发送失败', 4: '驳回', 5: '已撤回' }
      return map[val] || '未知'
    }
  }
}
</script>

<style>
.el-table .row-failed { background-color: #fef0f0 !important; }
.el-table .row-failed:hover > td { background-color: #fde2e2 !important; }
.gm-mail-preview p {
  margin: 0 0 4px 0 !important;
}
.gm-mail-preview h1, .gm-mail-preview h2, .gm-mail-preview h3 {
  margin: 4px 0 !important;
}
.detail-container { max-height: 70vh; overflow-y: auto; padding: 4px 0; }
.detail-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  background: #fafafa;
}
.detail-card-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.detail-table { width: 100%; border-collapse: collapse; }
.detail-table td { padding: 6px 8px; font-size: 13px; vertical-align: top; }
.dt-label { color: #909399; width: 80px; white-space: nowrap; }
.dt-value { color: #303133; }
</style>
