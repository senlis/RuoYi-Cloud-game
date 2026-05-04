<template>
  <div class="app-container">
    <el-form ref="form" :model="form" label-width="100px">
      <el-form-item label="选择角色" prop="roleId">
        <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 300px" @change="handleRoleChange">
          <el-option
            v-for="role in roleOptions"
            :key="role.roleId"
            :label="role.roleName"
            :value="role.roleId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="实体类型" prop="entityType">
        <el-select v-model="form.entityType" placeholder="请选择实体类型" style="width: 300px" @change="handleEntityTypeChange">
          <el-option
            v-for="dict in dict.type.game_entity_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="授权实体" prop="authIds">
        <el-transfer
          v-model="form.authIds"
          :data="entityList"
          :titles="['未授权', '已授权']"
          :props="{ key: 'key', label: 'label' }"
          filterable
          filter-placeholder="请输入名称搜索"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">保 存</el-button>
        <el-button @click="resetForm">重 置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { listRole } from "@/api/system/role"
import { getAuthByRole, saveRoleAuth } from "@/api/game/roleAuth"
import { listProject } from "@/api/game/project"
import { listChannel } from "@/api/game/channel"
import { listRegion } from "@/api/game/region"

export default {
  name: "GameRoleAuth",
  dicts: ['game_entity_type'],
  data() {
    return {
      // 角色选项
      roleOptions: [],
      // 实体穿梭框数据
      entityList: [],
      // 提交loading
      submitLoading: false,
      // 表单
      form: {
        roleId: undefined,
        entityType: undefined,
        authIds: []
      }
    }
  },
  created() {
    this.loadRoles()
  },
  methods: {
    /** 加载角色列表 */
    loadRoles() {
      listRole({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.roleOptions = response.rows || []
      })
    },
    /** 角色变更 */
    handleRoleChange() {
      this.form.authIds = []
      if (this.form.roleId && this.form.entityType) {
        this.loadAuthData()
      }
    },
    /** 实体类型变更 */
    handleEntityTypeChange() {
      this.form.authIds = []
      this.loadEntityOptions()
      if (this.form.roleId) {
        this.loadAuthData()
      }
    },
    /** 加载实体选项列表 */
    loadEntityOptions() {
      const type = this.form.entityType
      if (!type) {
        this.entityList = []
        return
      }
      const loadFn = type === 'project' ? listProject
        : type === 'channel' ? listChannel
        : type === 'region' ? listRegion
        : null
      if (!loadFn) return

      loadFn({ pageNum: 1, pageSize: 9999 }).then(response => {
        const rows = response.rows || []
        this.entityList = rows.map(item => {
          const key = type === 'project' ? item.projectId
            : type === 'channel' ? item.channelId
            : item.regionId
          const label = type === 'project' ? item.projectName
            : type === 'channel' ? item.channelName
            : item.regionName
          return { key, label }
        })
      })
    },
    /** 加载已授权数据 */
    loadAuthData() {
      if (!this.form.roleId || !this.form.entityType) return
      getAuthByRole(this.form.roleId, this.form.entityType).then(response => {
        this.form.authIds = response.data || []
      })
    },
    /** 重置 */
    resetForm() {
      this.form.roleId = undefined
      this.form.entityType = undefined
      this.form.authIds = []
      this.entityList = []
    },
    /** 提交保存 */
    submitForm() {
      if (!this.form.roleId) {
        this.$modal.msgError("请选择角色")
        return
      }
      if (!this.form.entityType) {
        this.$modal.msgError("请选择实体类型")
        return
      }
      this.submitLoading = true
      saveRoleAuth({
        roleId: this.form.roleId,
        entityType: this.form.entityType,
        entityIds: this.form.authIds
      }).then(() => {
        this.$modal.msgSuccess("保存成功")
        this.submitLoading = false
      }).catch(() => {
        this.submitLoading = false
      })
    }
  }
}
</script>
