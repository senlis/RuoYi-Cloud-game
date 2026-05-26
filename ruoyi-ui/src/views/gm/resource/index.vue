<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="搜索" clearable style="width:200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:140px">
          <el-option label="待审批" :value="0" /><el-option label="已发放" :value="2" />
          <el-option label="发放失败" :value="3" /><el-option label="驳回" :value="4" />
          <el-option label="已撤回" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="mini" @click="handleQuery">搜索</el-button>
        <el-button size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['gm:resource:add']">新建申请</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" :row-class-name="rowClassName">
      <el-table-column label="ID" prop="requestId" width="70" />
      <el-table-column label="标题" prop="title" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="类型" width="70">
        <template slot-scope="s">{{ typeLabels[s.row.requestType] }}</template>
      </el-table-column>
      <el-table-column label="资源内容" min-width="200" :show-overflow-tooltip="true">
        <template slot-scope="s">{{ resourcesSummary(s.row.resources) }}</template>
      </el-table-column>
      <el-table-column label="申请人" prop="applicant" width="100" />
      <el-table-column label="状态" width="90">
        <template slot-scope="s"><el-tag :type="statusTag(s.row.status)">{{ statusLabels[s.row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template slot-scope="s">{{ parseTime(s.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template slot-scope="s">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(s.row)">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(s.row)" v-if="s.row.status === 0 || s.row.status === 4">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(s.row)" v-if="s.row.status === 0 || s.row.status === 4">删除</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh" @click="handleRetry(s.row)" v-if="s.row.failedServerIds" style="color:#e6a23c">重推</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 查看详情 -->
    <el-dialog title="申请详情" :visible.sync="detailOpen" width="720px" append-to-body top="5vh">
      <div class="detail-container">
        <div class="detail-card"><div class="detail-card-title">基本信息</div>
          <table class="detail-table">
            <tr><td class="dt-label">标题</td><td class="dt-value" colspan="3">{{ detail.title }}</td></tr>
            <tr>
              <td class="dt-label">类型</td><td class="dt-value">{{ typeLabels[detail.requestType] }}</td>
              <td class="dt-label" style="width:90px">紧急</td><td class="dt-value">{{ detail.urgency === 1 ? '紧急' : '普通' }}</td>
            </tr>
            <tr>
              <td class="dt-label">申请人</td><td class="dt-value">{{ detail.applicant }}</td>
              <td class="dt-label">审批人</td><td class="dt-value">{{ detail.approver || '-' }}</td>
            </tr>
          </table>
        </div>
        <div class="detail-card"><div class="detail-card-title">目标信息</div>
          <table class="detail-table">
            <tr><td class="dt-label">项目</td><td class="dt-value"><b>{{ detailProjectName || '加载中…' }}</b> <span style="color:#909399;font-size:12px">(ID: {{ detail.projectId }})</span></td></tr>
            <tr><td class="dt-label">角色ID</td><td class="dt-value">{{ detail.playerIds }}</td></tr>
            <tr v-if="detailServerNames.length > 0">
              <td class="dt-label">服务器</td>
              <td class="dt-value">
                <div style="display:flex;flex-wrap:wrap;gap:4px">
                  <el-tag v-for="s in detailServerNames" :key="s"
                    :type="detail.failedServerIds && detail.failedServerIds.split(',').includes(s.split('(').pop().replace(')','')) ? 'danger' : 'success'"
                    size="small">{{ s }}</el-tag>
                </div>
              </td>
            </tr>
            <tr v-if="detail.failedServerIds">
              <td class="dt-label">推送</td>
              <td class="dt-value"><el-tag type="danger">部分服务器推送失败</el-tag></td>
            </tr>
          </table>
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
  </div>
</template>

<script>
import { listResource, getResource, delResource, retryResource } from "@/api/gm/resource"
import { getProject } from "@/api/game/project"
import { listServer } from "@/api/game/server"

export default {
  name: "ResourceRequest",
  data() {
    return {
      loading: false, showSearch: true, total: 0, list: [],
      queryParams: { pageNum: 1, pageSize: 10, title: undefined, status: undefined },
      detailOpen: false, detail: {}, detailResources: "", detailProjectName: "", detailServerNames: [],
      typeLabels: { 0:'道具',1:'货币',2:'等级',3:'创建角色',4:'自定义' },
      statusLabels: { 0:'待审批',1:'审批通过',2:'已发放',3:'发放失败',4:'驳回',5:'已撤回' }
    }
  },
  created() { this.getList() },
  activated() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listResource(this.queryParams).then(r => { this.list = r.rows; this.total = r.total; this.loading = false })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleAdd() { this.$router.push('/gm/resource/add') },
    handleEdit(row) { this.$router.push('/gm/resource/edit/' + row.requestId) },
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
    handleDelete(row) {
      this.$modal.confirm('确定删除？仅草稿或驳回状态可删除。').then(() => delResource(row.requestId)).then(() => { this.getList(); this.$modal.msgSuccess("已删除") }).catch(() => {})
    },
    handleRetry(row) {
      this.$modal.confirm('确定重推失败服务器？').then(() => retryResource(row.requestId)).then(() => { this.getList(); this.$modal.msgSuccess("重推完成") }).catch(() => {})
    },
    resourcesSummary(resources) {
      if (!resources) return ''
      try {
        const list = JSON.parse(resources)
        return list.map(r => (r.itemName || r.type) + (r.count ? 'x' + r.count : '')).join('、')
      } catch(e) { return resources }
    },
    statusTag(s) { return { 0:'warning', 1:'primary', 2:'success', 3:'danger', 4:'info', 5:'info' }[s] || 'info' },
    rowClassName({ row }) { return row.failedServerIds ? 'row-failed' : '' }
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
