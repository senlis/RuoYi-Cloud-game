<template>
  <div class="app-container">
    <el-page-header @back="goBack" :content="pageTitle" style="margin-bottom:16px" />

    <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">

      <!-- 服务器选择（顶部） -->
      <el-form-item label="服务器">
        <ServerSelector v-model="serverSelection" :showDatePicker="false" />
      </el-form-item>

      <el-form-item label="邮件标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入邮件标题" maxlength="128" show-word-limit />
      </el-form-item>

      <el-form-item label="邮件内容" prop="content">
        <RichEditor v-model="form.content" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="发送方式">
            <el-radio-group v-model="form.sendType">
              <el-radio :label="0">立即发送</el-radio>
              <el-radio :label="1">定时发送</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="8" v-if="form.sendType === 1">
          <el-form-item label="定时时间" prop="sendTime">
            <el-date-picker v-model="form.sendTime" type="datetime" placeholder="选择发送时间"
              value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="有效期(天)" prop="expireDays">
            <el-input-number v-model="form.expireDays" :min="1" :max="365" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="目标范围">
        <el-radio-group v-model="form.targetType">
          <el-radio :label="0">全服</el-radio>
          <el-radio :label="1">条件筛选</el-radio>
          <el-radio :label="2">指定玩家</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 条件筛选 -->
      <el-row v-if="form.targetType === 1" :gutter="20">
        <el-col :span="12">
          <el-form-item label="等级范围">
            <div style="display:flex;align-items:center;gap:6px;">
              <el-input-number v-model="form.minLevel" :min="1" :max="999" :controls="false" style="flex:1" placeholder="最低" />
              <span style="color:#909399;flex-shrink:0;">~</span>
              <el-input-number v-model="form.maxLevel" :min="1" :max="999" :controls="false" style="flex:1" placeholder="最高" />
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="VIP范围">
            <div style="display:flex;align-items:center;gap:6px;">
              <el-input-number v-model="form.minVip" :min="0" :max="99" :controls="false" style="flex:1" placeholder="最低" />
              <span style="color:#909399;flex-shrink:0;">~</span>
              <el-input-number v-model="form.maxVip" :min="0" :max="99" :controls="false" style="flex:1" placeholder="最高" />
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 指定玩家 -->
      <el-form-item v-if="form.targetType === 2" label="指定玩家" prop="targetPlayers">
        <el-input v-model="form.targetPlayers" type="textarea" rows="4"
          placeholder="每行输入一个角色ID（playerId），多个用换行分隔" />
      </el-form-item>

      <!-- 附件奖励 -->
      <el-form-item label="附件奖励">
        <ItemSelector v-model="rewardList" :projectId="serverSelection.projectId" />
      </el-form-item>

    </el-form>

    <!-- 底部操作栏 -->
    <div style="border-top:1px solid #ebeef5;padding:16px 0;margin-top:16px;text-align:center">
      <el-button type="primary" size="medium" @click="submitForm" :loading="saving">保存</el-button>
      <el-button size="medium" @click="goBack">取消</el-button>
    </div>
  </div>
</template>

<script>
import { getGmMail, addGmMail, updateGmMail } from "@/api/gm/mail"
import ServerSelector from "@/components/game/ServerSelector"
import ItemSelector from "@/components/game/ItemSelector"
import RichEditor from "@/components/game/RichEditor"

export default {
  name: "GmMailForm",
  components: { ServerSelector, ItemSelector, RichEditor },
  data() {
    return {
      pageTitle: "新建GM邮件",
      isEdit: false,
      saving: false,
      form: {
        mailId: undefined,
        title: undefined,
        content: "",
        sendType: 0,
        sendTime: undefined,
        expireDays: 7,
        targetType: 0,
        targetPlayers: undefined,
        minLevel: undefined,
        maxLevel: undefined,
        minVip: undefined,
        maxVip: undefined,
        serverIds: undefined,
        projectId: undefined,
        rewards: undefined
      },
      serverSelection: { projectId: null, channelIds: [], regionIds: [], serverIds: [] },
      rewardList: [],
      rules: {
        title: [{ required: true, message: "邮件标题不能为空", trigger: "blur" }]
      }
    }
  },
  watch: {
    $route: {
      immediate: true,
      handler(to, from) {
        const isGmMail = p => p === '/gm/mail/add' || p.startsWith('/gm/mail/edit/')
          || p === '/gm/mail' || p.startsWith('/gm/mail/')
        const toIsGm = isGmMail(to.path)
        const fromIsGm = from ? isGmMail(from.path) : false

        // 首次加载（immediate），初始化页面
        if (!from) {
          const mailId = to.params && to.params.mailId
          this.isEdit = !!mailId
          this.pageTitle = mailId ? "编辑GM邮件" : "新建GM邮件"
          if (mailId) this.$nextTick(() => this.loadMail(mailId))
          return
        }

        // 从非GM邮件页面切回时保留数据
        if (toIsGm && !fromIsGm) return

        // GM邮件路由间切换时重置并加载
        if (toIsGm && fromIsGm) {
          this.resetFormData()
          const mailId = to.params && to.params.mailId
          if (mailId) {
            this.isEdit = true
            this.pageTitle = "编辑GM邮件"
            this.$nextTick(() => this.loadMail(mailId))
          } else {
            this.isEdit = false
            this.pageTitle = "新建GM邮件"
          }
        }
      }
    }
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    },
    resetFormData() {
      this.form = {
        mailId: undefined, title: undefined, content: "",
        sendType: 0, sendTime: undefined, expireDays: 7,
        targetType: 0, targetPlayers: undefined,
        minLevel: undefined, maxLevel: undefined,
        minVip: undefined, maxVip: undefined,
        serverIds: undefined, projectId: undefined, rewards: undefined
      }
      this.serverSelection = { projectId: null, channelIds: [], regionIds: [], serverIds: [] }
      this.rewardList = []
      this.isEdit = false
    },
    loadMail(mailId) {
      getGmMail(mailId).then(response => {
        const data = response.data
        if (!data) { this.$modal.msgError("邮件数据为空"); return }
        // 逐个字段赋值，确保表单响应式更新
        this.form.mailId = data.mailId
        this.form.title = data.title
        this.form.content = data.content || ''
        this.form.sendType = data.sendType !== undefined ? data.sendType : 0
        this.form.sendTime = data.sendTime
        this.form.expireDays = data.expireDays || 7
        this.form.targetType = data.targetType !== undefined ? data.targetType : 0
        this.form.targetPlayers = data.targetPlayers
        this.form.minLevel = data.minLevel
        this.form.maxLevel = data.maxLevel
        this.form.minVip = data.minVip
        this.form.maxVip = data.maxVip
        this.form.serverIds = data.serverIds
        this.form.projectId = data.projectId
        this.form.rewards = data.rewards
        // ServerSelector 内部未提供外部回填能力，需手动选服
        // 回填服务器选择（含渠道和分区）
        if (data.projectId) {
          this.serverSelection = {
            projectId: data.projectId,
            channelIds: data.channelIds ? data.channelIds.split(',').map(Number) : [],
            regionIds: data.regionIds ? data.regionIds.split(',').map(Number) : [],
            serverIds: data.serverIds ? data.serverIds.split(',').map(Number) : [],
            _forceRestore: true // 强制重新加载
          }
        }
        // 回填道具奖励
        if (data.rewards) {
          try { this.rewardList = JSON.parse(data.rewards) } catch(e) { this.rewardList = [] }
        }
      }).catch(() => {
        this.$modal.msgError("加载邮件数据失败")
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) return
        this.saving = true

        const data = { ...this.form }
        // 服务器
        data.projectId = this.serverSelection.projectId || undefined
        data.serverIds = this.serverSelection.serverIds.length > 0
          ? this.serverSelection.serverIds.join(',') : undefined
        data.channelIds = this.serverSelection.channelIds && this.serverSelection.channelIds.length > 0
          ? this.serverSelection.channelIds.join(',') : undefined
        data.regionIds = this.serverSelection.regionIds && this.serverSelection.regionIds.length > 0
          ? this.serverSelection.regionIds.join(',') : undefined
        // 奖励
        data.rewards = this.rewardList.length > 0 ? JSON.stringify(this.rewardList) : undefined

        const request = this.isEdit ? updateGmMail(data) : addGmMail(data)
        request.then(() => {
          this.$modal.msgSuccess(this.isEdit ? "修改成功" : "新增成功")
          this.goBack()
        }).catch(() => {
          this.$modal.msgError("保存失败")
        }).finally(() => {
          this.saving = false
        })
      })
    }
  }
}
</script>
