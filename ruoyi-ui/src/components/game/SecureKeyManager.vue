<template>
  <div>
    <el-divider content-position="left">SecureKey 管理</el-divider>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="状态">
          <el-tag v-if="secureKeyInfo.secureKeyConfigured" type="success">已配置</el-tag>
          <el-tag v-else type="danger">未配置</el-tag>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="版本号">
          <span>{{ secureKeyInfo.secureKeyVersion || '-' }}</span>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="24">
        <el-form-item label="最近更新">
          <span>{{ secureKeyInfo.secureKeyUpdatedAt || '-' }}</span>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="24">
        <el-button type="primary" size="small" @click="handleGenerate" :loading="generating">
          生成 SecureKey
        </el-button>
        <el-button type="warning" size="small" @click="handleReset" :loading="resetting">
          重置 SecureKey
        </el-button>
        <el-button size="small" @click="handleRefresh">
          刷新状态
        </el-button>
      </el-col>
    </el-row>

    <!-- SecureKey 展示对话框 -->
    <el-dialog title="SecureKey" :visible.sync="showKeyDialog" width="500px" append-to-body>
      <div style="text-align: center; padding: 20px;">
        <div style="margin-bottom: 15px; color: #e6a23c; font-weight: bold;">
          请立即保存此 SecureKey，关闭后将无法再次查看！
        </div>
        <el-input
          ref="secureKeyInput"
          :value="newSecureKey"
          type="textarea"
          :rows="3"
          readonly
          style="margin-bottom: 15px;"
        />
        <div style="display: flex; justify-content: center; gap: 10px;">
          <el-button type="primary" @click="copySecureKey">复制到剪贴板</el-button>
          <el-button @click="showKeyDialog = false">我已保存</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getSecureKeyInfo, generateSecureKey, resetSecureKey } from "@/api/game/channel";

export default {
  name: "SecureKeyManager",
  props: {
    channelId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      secureKeyInfo: {},
      generating: false,
      resetting: false,
      showKeyDialog: false,
      newSecureKey: ""
    };
  },
  watch: {
    channelId: {
      immediate: true,
      handler(val) {
        if (val) {
          this.loadSecureKeyInfo();
        }
      }
    }
  },
  methods: {
    async loadSecureKeyInfo() {
      try {
        const res = await getSecureKeyInfo(this.channelId);
        if (res && res.code === 200) {
          this.secureKeyInfo = res.data;
        }
      } catch (e) {
        console.error("获取 SecureKey 信息失败", e);
      }
    },
    async handleGenerate() {
      this.generating = true;
      try {
        const res = await generateSecureKey(this.channelId);
        if (res && res.code === 200) {
          this.newSecureKey = res.data.secureKey;
          this.showKeyDialog = true;
          this.loadSecureKeyInfo();
          this.$modal.msgSuccess("SecureKey 生成成功");
        } else {
          this.$modal.msgError(res.msg || "生成失败");
        }
      } catch (e) {
        this.$modal.msgError("生成失败");
      } finally {
        this.generating = false;
      }
    },
    async handleReset() {
      this.$modal.confirm("重置 SecureKey 后，旧 Key 将立即失效，游戏服需要更新配置。确定继续？").then(async () => {
        this.resetting = true;
        try {
          const res = await resetSecureKey(this.channelId);
          if (res && res.code === 200) {
            this.newSecureKey = res.data.secureKey;
            this.showKeyDialog = true;
            this.loadSecureKeyInfo();
            this.$modal.msgSuccess("SecureKey 重置成功");
          } else {
            this.$modal.msgError(res.msg || "重置失败");
          }
        } catch (e) {
          this.$modal.msgError("重置失败");
        } finally {
          this.resetting = false;
        }
      }).catch(() => {});
    },
    handleRefresh() {
      this.loadSecureKeyInfo();
    },
    copySecureKey() {
      this.$copyText(this.newSecureKey).then(() => {
        this.$modal.msgSuccess("已复制到剪贴板");
      }).catch(() => {
        // 降级方案
        const textarea = this.$refs.secureKeyInput.$el.querySelector("textarea");
        if (textarea) {
          textarea.select();
          document.execCommand("copy");
          this.$modal.msgSuccess("已复制到剪贴板");
        }
      });
    }
  }
};
</script>
