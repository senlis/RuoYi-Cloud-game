<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="搜索" clearable style="width:200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width:140px">
          <el-option label="待审批" :value="0" /><el-option label="已通过" :value="1" /><el-option label="已驳回" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="mini" @click="handleQuery">搜索</el-button>
        <el-button size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" prop="requestId" width="70" />
      <el-table-column label="标题" prop="title" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="申请人" prop="applicant" width="100" />
      <el-table-column label="状态" width="90">
        <template slot-scope="s"><el-tag :type="statusTag(s.row.status)">{{ statusLabels[s.row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template slot-scope="s">{{ parseTime(s.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template slot-scope="s">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(s.row)">详情</el-button>
          <el-button v-if="s.row.status === 0" size="mini" type="success" @click="handleApprove(s.row)" :loading="approving">通过</el-button>
          <el-button v-if="s.row.status === 0" size="mini" type="danger" @click="handleReject(s.row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 详情对话框 -->
    <el-dialog title="申请详情" :visible.sync="detailOpen" width="720px" append-to-body top="5vh">
      <div class="detail-container">
        <div class="detail-card"><div class="detail-card-title">基本信息</div>
          <table class="detail-table">
            <tr><td class="dt-label">标题</td><td class="dt-value" colspan="3">{{ detail.title }}</td></tr>
            <tr>
              <td class="dt-label">类型</td><td class="dt-value">{{ typeLabels[detail.requestType] }}</td>
              <td class="dt-label">紧急</td><td class="dt-value">{{ detail.urgency === 1 ? '紧急' : '普通' }}</td>
            </tr>
            <tr><td class="dt-label">申请人</td><td class="dt-value">{{ detail.applicant }}</td><td class="dt-label">审批人</td><td class="dt-value">{{ detail.approver || '-' }}</td></tr>
          </table>
        </div>
        <div class="detail-card"><div class="detail-card-title">目标信息</div>
          <div style="font-size:13px">
            <div>项目：<b>{{ detailProjectName || '加载中…' }}</b> <span style="color:#909399;font-size:12px">(ID: {{ detail.projectId }})</span></div>
            <div>角色ID：{{ detail.playerIds }}</div>
            <div v-if="detailServerNames.length > 0" style="margin-top:4px;display:flex;flex-wrap:wrap;gap:4px">
              <el-tag v-for="s in detailServerNames" :key="s"
                :type="detail.failedServerIds && detail.failedServerIds.split(',').includes(s.split('(').pop().replace(')','')) ? 'danger' : 'success'"
                size="small">{{ s }}</el-tag>
            </div>
            <div v-if="detail.failedServerIds" style="margin-top:4px"><el-tag type="danger">部分服务器推送失败</el-tag></div>
          </div>
        </div>
        <div class="detail-card"><div class="detail-card-title">资源内容</div>
          <pre style="margin:0;font-size:13px;background:#f5f7fa;padding:8px;border-radius:4px;white-space:pre-wrap">{{ detailResources || detail.resources }}</pre>
        </div>
        <div class="detail-card"><div class="detail-card-title">申请理由</div>
          <div style="font-size:13px;color:#606266">{{ detail.reason }}</div>
        </div>
        <div class="detail-card" v-if="detail.auditRemark"><div class="detail-card-title">审批意见</div>
          <el-tag type="danger">{{ detail.auditRemark }}</el-tag>
        </div>
      </div>
      <div slot="footer"><el-button @click="detailOpen = false">关 闭</el-button></div>
    </el-dialog>

    <el-dialog title="驳回原因" :visible.sync="rejectOpen" width="400px" append-to-body>
      <el-input v-model="rejectRemark" type="textarea" rows="4" placeholder="请输入驳回原因" />
      <div slot="footer">
        <el-button type="primary" @click="submitReject">确 定</el-button>
        <el-button @click="rejectOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listResource, getResource, approveResource, rejectResource } from "@/api/gm/resource"
import { getProject } from "@/api/game/project"
import { listServer } from "@/api/game/server"

export default {
  name: "ResourceAudit",
  data() {
    return {
      loading: false, showSearch: true, total: 0, list: [], approving: false,
      queryParams: { pageNum: 1, pageSize: 10, title: undefined, status: 0 },
      detailOpen: false, detail: {}, detailResources: "", detailProjectName: "", detailServerNames: [],
      rejectOpen: false, rejectId: null, rejectRemark: "",
      typeLabels: { 0:'道具',1:'货币',2:'等级',3:'创建角色',4:'自定义' },
      statusLabels: { 0:'待审批',1:'已通过',4:'已驳回' }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listResource(this.queryParams).then(r => { this.list = r.rows; this.total = r.total; this.loading = false })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.queryParams.status = 0; this.resetForm("queryForm"); this.handleQuery() },
    handleView(row) {
      getResource(row.requestId).then(r => {
        this.detail = r.data
        this.detailProjectName = ""
        this.detailServerNames = []
        try { const p = JSON.parse(r.data.resources || '[]'); this.detailResources = p.map(v => (v.itemName || v.type) + ' x' + (v.count || v.remark || '')).join('\n') } catch(e) { this.detailResources = r.data.resources }
        const d = r.data
        if (d.projectId) {
          getProject(d.projectId).then(r2 => { this.detailProjectName = r2.data ? r2.data.projectName : '' }).catch(() => {})
        }
        if (d.serverIds) {
          const ids = d.serverIds.split(',').map(Number)
          listServer({ pageSize: 999 }).then(r2 => {
            const allServers = r2.rows || []
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
      this.$modal.confirm('通过后系统将自动发放资源，确定通过？').then(() => {
        this.approving = true
        return approveResource(row.requestId)
      }).then(res => {
        this.approving = false; this.getList()
        const msg = res.failedServerIds ? '已通过，但以下服务器发送失败：' + res.failedServerIds : '审批通过'
        res.failedServerIds ? this.$alert(msg, '部分失败', { type: 'warning' }) : this.$modal.msgSuccess(msg)
      }).catch(() => { this.approving = false })
    },
    handleReject(row) { this.rejectId = row.requestId; this.rejectRemark = ""; this.rejectOpen = true },
    submitReject() {
      if (!this.rejectRemark) return this.$modal.msgWarning("请输入驳回原因")
      rejectResource(this.rejectId, this.rejectRemark).then(() => { this.rejectOpen = false; this.getList(); this.$modal.msgSuccess("已驳回") }).catch(() => {})
    },
    statusTag(s) { return { 0:'warning', 1:'success', 4:'danger' }[s] || 'info' }
  }
}
</script>

<style scoped>
.detail-container { max-height: 70vh; overflow-y: auto; }
.detail-card { border:1px solid #ebeef5; border-radius:6px; padding:16px; margin-bottom:12px; background:#fafafa; }
.detail-card-title { font-size:14px; font-weight:bold; margin-bottom:10px; padding-bottom:8px; border-bottom:1px solid #ebeef5; }
.detail-table { width:100%; border-collapse:collapse; }
.detail-table td { padding:6px 8px; font-size:13px; }
.dt-label { color:#909399; width:80px; white-space:nowrap; }
.dt-value { color:#303133; }
</style>
