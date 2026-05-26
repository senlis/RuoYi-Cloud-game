<template>
  <div class="app-container">
    <el-page-header @back="goBack" :content="pageTitle" style="margin-bottom:16px" />

    <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
      <el-form-item label="服务器">
        <ServerSelector v-model="serverSelection" :showDatePicker="false" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="申请标题" prop="title">
            <el-input v-model="form.title" placeholder="请简要说明申请内容" maxlength="128" show-word-limit />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="申请类型">
            <el-select v-model="form.requestType" style="width:100%">
              <el-option label="道具发放" :value="0" />
              <el-option label="货币调整" :value="1" />
              <el-option label="等级调整" :value="2" />
              <el-option label="创建角色" :value="3" />
              <el-option label="自定义" :value="4" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="紧急程度">
            <el-radio-group v-model="form.urgency">
              <el-radio :label="0">普通</el-radio>
              <el-radio :label="1">紧急</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="目标角色" prop="playerIds">
        <el-input v-model="form.playerIds" type="textarea" rows="3" placeholder="每行输入一个角色ID（playerId）" />
      </el-form-item>

      <!-- 道具发放：使用快捷选择器 -->
      <el-form-item label="资源内容" v-if="form.requestType === 0">
        <div v-if="!serverSelection.projectId" style="color:#909399;font-size:13px;padding:8px 0">
          请先在上方选择项目，然后使用道具选择器搜索添加道具
        </div>
        <ItemSelector v-model="resourceList" :projectId="serverSelection.projectId" v-else />
      </el-form-item>

      <!-- 其他类型：JSON 编辑 -->
      <el-form-item label="资源内容" v-else>
        <el-input v-model="form.resources" type="textarea" rows="3" placeholder='JSON格式：例如 [{"type":"level","remark":"99"}]' />
      </el-form-item>

      <el-form-item label="申请理由" prop="reason">
        <el-input v-model="form.reason" type="textarea" rows="3" placeholder="请说明申请原因和用途" />
      </el-form-item>

      <el-form-item label="审批人">
        <el-select v-model="form.approver" filterable placeholder="选择审批人（有项目权限+审批权限）"
          :loading="userLoading" style="width:100%">
          <el-option v-for="u in userOptions" :key="u.userName" :label="u.nickName + '(' + u.userName + ')'" :value="u.userName" />
          <div v-if="userOptions.length === 0 && serverSelection.projectId" style="padding:8px;color:#909399;font-size:12px;text-align:center">无符合条件的审批人</div>
        </el-select>
      </el-form-item>
    </el-form>

    <div style="border-top:1px solid #ebeef5;padding:16px 0;margin-top:16px;text-align:center">
      <el-button type="primary" size="medium" @click="submitForm" :loading="saving">提交申请</el-button>
      <el-button size="medium" @click="goBack">取消</el-button>
    </div>
  </div>
</template>

<script>
import { getResource, addResource, updateResource, getApprovers } from "@/api/gm/resource"
import ServerSelector from "@/components/game/ServerSelector"
import ItemSelector from "@/components/game/ItemSelector"

export default {
  name: "ResourceForm",
  components: { ServerSelector, ItemSelector },
  data() {
    return {
      pageTitle: "新建资源申请",
      isEdit: false,
      saving: false,
      userLoading: false,
      userOptions: [],
      form: {
        requestId: undefined, title: "", requestType: 0,
        projectId: undefined, serverIds: undefined,
        playerIds: "", resources: "", reason: "",
        urgency: 0, approver: ""
      },
      serverSelection: { projectId: null, channelIds: [], regionIds: [], serverIds: [] },
      resourceList: [],
      rules: {
        title: [{ required: true, message: "申请标题不能为空", trigger: "blur" }],
        playerIds: [{ required: true, message: "请填写目标角色ID", trigger: "blur" }],
        reason: [{ required: true, message: "请填写申请理由", trigger: "blur" }]
      }
    }
  },
  watch: {
    'serverSelection.projectId'(val) {
      if (val) {
        this.userLoading = true
        getApprovers(val).then(r => { this.userOptions = r.data || [] }).catch(() => {}).finally(() => { this.userLoading = false })
      } else {
        this.userOptions = []
      }
    }
  },
  created() {
    const id = this.$route.params && this.$route.params.requestId
    if (id) {
      this.isEdit = true
      this.pageTitle = "编辑资源申请"
      this.loadResource(id)
    }
  },
  methods: {
    resetFormData() {
      this.form = {
        requestId: undefined, title: "", requestType: 0,
        projectId: undefined, serverIds: undefined, channelIds: undefined, regionIds: undefined,
        playerIds: "", resources: "", reason: "", urgency: 0, approver: ""
      }
      this.serverSelection = { projectId: null, channelIds: [], regionIds: [], serverIds: [] }
      this.resourceList = []
      this.isEdit = false
    },
    loadResource(id) {
      console.log('[ResourceForm] loadResource id=', id)
      getResource(id).then(r => {
        const d = r.data
        console.log('[ResourceForm] API data:', d ? `title=${d.title} status=${d.status}` : 'EMPTY')
        if (!d) { this.$modal.msgError("加载失败：数据为空"); return }
        this.form = { ...d }
        this.form.requestId = d.requestId
        if (d.projectId) {
          this.serverSelection = {
            projectId: d.projectId,
            channelIds: [],
            regionIds: [],
            serverIds: d.serverIds ? d.serverIds.split(',').map(Number) : [],
            _forceRestore: true
          }
        }
        if (d.requestType === 0 && d.resources) {
          try { this.resourceList = JSON.parse(d.resources) } catch(e) {}
        }
        console.log('[ResourceForm] loadResource done')
      }).catch(e => {
        console.error('[ResourceForm] loadResource error:', e)
        this.$modal.msgError("加载数据失败")
      })
    },
    goBack() { this.$router.go(-1) },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.saving = true
        const data = { ...this.form }
        data.projectId = this.serverSelection.projectId || undefined
        data.serverIds = this.serverSelection.serverIds.length > 0
          ? this.serverSelection.serverIds.join(',') : undefined
        data.channelIds = this.serverSelection.channelIds && this.serverSelection.channelIds.length > 0
          ? this.serverSelection.channelIds.join(',') : undefined
        data.regionIds = this.serverSelection.regionIds && this.serverSelection.regionIds.length > 0
          ? this.serverSelection.regionIds.join(',') : undefined
        if (data.requestType === 0) {
          data.resources = this.resourceList.length > 0 ? JSON.stringify(this.resourceList) : undefined
        }
        (this.isEdit ? updateResource(data) : addResource(data)).then(() => {
          this.$modal.msgSuccess(this.isEdit ? "修改成功" : "提交成功")
          this.goBack()
        }).catch(() => {}).finally(() => { this.saving = false })
      })
    }
  }
}
</script>
