<template>
  <div class="app-container">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="邮件标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入邮件标题" clearable style="width: 240px"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 240px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 审核列表 -->
    <el-table v-loading="loading" :data="auditList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="mailId" width="70" />
      <el-table-column label="邮件标题" align="center" prop="title" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="发送方式" align="center" width="80">
        <template slot-scope="scope">{{ scope.row.sendType === 0 ? '立即' : '定时' }}</template>
      </el-table-column>
      <el-table-column label="创建人" align="center" prop="createdBy" width="120" />
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="180">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === 0" size="mini" type="success" icon="el-icon-check"
            @click="handleApprove(scope.row)" v-hasPermi="['gm:mail:audit']"
            :loading="approving">通过</el-button>
          <el-button v-if="scope.row.status === 0" size="mini" type="danger" icon="el-icon-close"
            @click="handleReject(scope.row)" v-hasPermi="['gm:mail:audit']">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 详情对话框 -->
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
            v-html="detail.content"></div>
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

    <!-- 驳回对话框 -->
    <el-dialog title="驳回原因" :visible.sync="rejectOpen" width="400px" append-to-body>
      <el-input v-model="rejectRemark" type="textarea" rows="4" placeholder="请输入驳回原因" />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReject">确 定</el-button>
        <el-button @click="rejectOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAuditGmMail, getGmMail, approveGmMail, rejectGmMail } from "@/api/gm/mail"
import { getProject } from "@/api/game/project"
import { listServer } from "@/api/game/server"

export default {
  name: "GmMailAudit",
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      auditList: [],
      ids: [],
      single: true,
      multiple: true,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        status: 0 // 默认待审核
      },
      // 详情
      detailOpen: false,
      detail: {},
      detailRewards: [],
      detailProjectName: "",
      detailServerNames: [],
      approving: false,
      // 驳回
      rejectOpen: false,
      rejectMailId: null,
      rejectRemark: ""
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listAuditGmMail(this.queryParams).then(response => {
        this.auditList = response.rows
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
      this.queryParams.status = 0
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleView(row) {
      getGmMail(row.mailId).then(response => {
        this.detail = response.data
        this.detailProjectName = ""
        this.detailServerNames = []
        try { this.detailRewards = JSON.parse(response.data.rewards || '[]') } catch(e) { this.detailRewards = [] }
        const data = response.data
        if (data.projectId) {
          getProject(data.projectId).then(r => { this.detailProjectName = r.data ? r.data.projectName : '' }).catch(() => {})
        }
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
    handleApprove(row) {
      this.$modal.confirm('确定通过该邮件审核？通过后' + (row.sendType === 0 ? '将立即发送。' : '等待定时发送。')).then(() => {
        this.approving = true
        const msg = this.$modal.loading ? this.$modal.loading('正在推送中…') : null
        return approveGmMail(row.mailId)
      }).then(response => {
        this.approving = false
        if (this.$modal.closeLoading) this.$modal.closeLoading()
        this.getList()
        const failedIds = response.failedServerIds
        if (failedIds) {
          this.$alert('审核通过，但以下服务器推送失败：' + failedIds, '部分失败', { type: 'warning', confirmButtonText: '知道了' })
        } else {
          this.$modal.msgSuccess("全部服务器推送成功")
        }
      }).catch(() => {
        this.approving = false
        if (this.$modal.closeLoading) this.$modal.closeLoading()
      })
    },
    handleReject(row) {
      this.rejectMailId = row.mailId
      this.rejectRemark = ""
      this.rejectOpen = true
    },
    submitReject() {
      if (!this.rejectRemark) {
        this.$modal.msgWarning("请输入驳回原因")
        return
      }
      rejectGmMail(this.rejectMailId, this.rejectRemark).then(() => {
        this.rejectOpen = false
        this.getList()
        this.$modal.msgSuccess("已驳回")
      }).catch(() => {})
    },
    targetTypeLabel(val) {
      const map = { 0: '全服', 1: '条件筛选', 2: '指定玩家' }
      return map[val] || '未知'
    },
    statusTag(val) {
      const map = { 0: 'warning', 1: 'success', 4: 'danger' }
      return map[val] || 'info'
    },
    statusLabel(val) {
      const map = { 0: '待审核', 1: '已通过', 4: '已驳回' }
      return map[val] || '未知'
    }
  }
}
</script>

<style>
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
