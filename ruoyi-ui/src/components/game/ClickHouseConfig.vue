<template>
  <div>
    <el-divider content-position="left">ClickHouse 数据源配置</el-divider>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="主机地址" prop="clickhouseHost">
          <el-input
            v-model="config.host"
            placeholder="如 192.168.1.100"
            @input="emitConfig"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="端口号" prop="clickhousePort">
          <el-input-number
            v-model="config.port"
            :min="1"
            :max="65535"
            controls-position="right"
            @change="emitConfig"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="数据库名" prop="clickhouseDatabase">
          <el-input
            v-model="config.database"
            placeholder="如 game_analytics_mhxx"
            @input="emitConfig"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="用户名" prop="clickhouseUsername">
          <el-input
            v-model="config.username"
            placeholder="default"
            @input="emitConfig"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="密码" prop="clickhousePassword">
          <el-input
            v-model="config.password"
            type="password"
            show-password
            placeholder="密码（可为空）"
            @input="emitConfig"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="最大连接数" prop="clickhouseMaxPool">
          <el-input-number
            v-model="config.maxPoolSize"
            :min="1"
            :max="50"
            controls-position="right"
            @change="emitConfig"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="最小连接数" prop="clickhouseMinPool">
          <el-input-number
            v-model="config.minPoolSize"
            :min="1"
            :max="10"
            controls-position="right"
            @change="emitConfig"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label=" ">
          <el-button type="primary" size="mini" @click="testConnection" :loading="testing">
            测试连接
          </el-button>
          <span v-if="testResult !== null" :style="{ color: testResult ? '#67c23a' : '#f56c6c', marginLeft: '10px' }">
            {{ testResult ? '连接成功' : '连接失败' }}
          </span>
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { testClickHouseConnection } from "@/api/game/project";

export default {
  name: "ClickHouseConfig",
  props: {
    value: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      config: {
        host: "",
        port: 8123,
        database: "",
        username: "default",
        password: "",
        minPoolSize: 2,
        maxPoolSize: 8
      },
      testing: false,
      testResult: null
    };
  },
  watch: {
    value: {
      immediate: true,
      handler(val) {
        if (val && typeof val === 'object' && Object.keys(val).length > 0) {
          // 完全替换，避免项目切换时残留旧数据
          this.config = {
            host: val.host || "",
            port: val.port || 8123,
            database: val.database || "",
            username: val.username || "default",
            password: val.password || "",
            minPoolSize: val.minPoolSize || 2,
            maxPoolSize: val.maxPoolSize || 8
          };
        } else {
          // 无数据时重置为默认值
          this.config = { host: "", port: 8123, database: "", username: "default", password: "", minPoolSize: 2, maxPoolSize: 8 };
        }
      }
    }
  },
  methods: {
    emitConfig() {
      this.$emit("input", { ...this.config });
    },
    async testConnection() {
      this.testing = true;
      this.testResult = null;
      try {
        const res = await testClickHouseConnection(this.config);
        this.testResult = res && res.code === 200;
      } catch (e) {
        this.testResult = false;
      } finally {
        this.testing = false;
      }
    }
  }
};
</script>
