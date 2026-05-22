<template>
  <div class="css-root">
    <!-- 渠道（可多选） -->
    <div class="css-item">
      <span class="css-label">渠道</span>
      <el-select v-model="selectedChannels" multiple collapse-tags placeholder="请选择渠道"
        @change="onChannelsChange">
        <el-option
          v-for="item in channelOptions"
          :key="item.channelCode"
          :label="item.channelName"
          :value="item.channelCode"
        />
      </el-select>
    </div>

    <!-- 分区（可多选，只显示选中渠道下的分区） -->
    <div class="css-item">
      <span class="css-label">分区</span>
      <el-select v-model="selectedRegions" multiple collapse-tags
        :placeholder="channelPlaceholder"
        :disabled="selectedChannels.length === 0"
        @change="onRegionsChange">
        <el-option
          v-for="item in filteredRegions"
          :key="item.regionCode"
          :label="item.regionName"
          :value="item.regionCode"
        />
      </el-select>
    </div>

    <!-- 服务器（可多选，只显示选中分区下的服务器） -->
    <div class="css-item">
      <span class="css-label">服务器</span>
      <el-select v-model="selectedServers" multiple collapse-tags
        :placeholder="serverPlaceholder"
        :disabled="selectedRegions.length === 0"
        @change="emitChange">
        <el-option
          v-for="item in filteredServers"
          :key="item.serverId"
          :label="item.serverName"
          :value="item.serverId"
        />
      </el-select>
    </div>
  </div>
</template>

<script>
import { listChannelOptions, listRegionOptions } from "@/api/bridge/channelArg"
import { listServerByRegion } from "@/api/game/server"

export default {
  name: "ChannelServerSelector",
  props: {
    value: { type: Object, default: () => ({ channelKeys: [], serverIds: [] }) }
  },
  data() {
    return {
      channelOptions: [],
      // 全部已加载的分区，每个对象包含 regionId, regionCode, regionName, channelCode
      allRegions: [],
      // 全部已加载的服务器，每个对象包含 serverId, serverName, regionCode
      allServers: [],
      // 选中值
      selectedChannels: [],
      selectedRegions: [],
      selectedServers: []
    }
  },
  computed: {
    channelPlaceholder() {
      return this.selectedChannels.length === 0 ? '请先选择渠道' : '请选择分区'
    },
    serverPlaceholder() {
      return this.selectedRegions.length === 0 ? '请先选择分区' : '请选择服务器'
    },
    /** 只显示选中渠道下的分区 */
    filteredRegions() {
      if (this.selectedChannels.length === 0) return []
      return this.allRegions.filter(r => this.selectedChannels.includes(r.channelCode))
    },
    /** 只显示选中分区下的服务器 */
    filteredServers() {
      if (this.selectedRegions.length === 0) return []
      return this.allServers.filter(s => this.selectedRegions.includes(s.regionCode))
    }
  },
  watch: {
    // 初始化时同步父组件 v-model 的值
    value: {
      immediate: true,
      handler(val) {
        if (val && Array.isArray(val.channelKeys)) {
          this.selectedChannels = [...val.channelKeys]
        }
        if (val && Array.isArray(val.serverIds)) {
          this.selectedServers = [...val.serverIds]
        }
      }
    }
  },
  created() {
    this.loadChannels()
  },
  methods: {
    /** 加载渠道列表 */
    loadChannels() {
      listChannelOptions().then(res => {
        this.channelOptions = res.data || []
      })
    },

    /** 渠道选中变化时，加载对应分区 */
    onChannelsChange() {
      this.selectedRegions = []
      this.selectedServers = []
      if (this.selectedChannels.length === 0) {
        this.allRegions = []
        this.allServers = []
        this.emitChange()
        return
      }
      // 并发加载所有选中渠道的分区
      const promises = this.selectedChannels.map(channelCode =>
        listRegionOptions(channelCode).then(res => {
          const regions = res.data || []
          return regions.map(r => ({ ...r, channelCode }))
        })
      )
      Promise.all(promises).then(results => {
        this.allRegions = results.flat()
        this.emitChange()
      })
    },

    /** 分区选中变化时，加载对应服务器 */
    onRegionsChange() {
      this.selectedServers = []
      if (this.selectedRegions.length === 0) {
        this.allServers = []
        this.emitChange()
        return
      }
      // 从选中分区中找到 regionId，并发加载服务器
      const selectedRegionIds = this.allRegions
        .filter(r => this.selectedRegions.includes(r.regionCode))
        .map(r => r.regionId)
        .filter(id => id != null)

      if (selectedRegionIds.length === 0) {
        this.allServers = []
        this.emitChange()
        return
      }

      // 构建 regionCode → regionId 映射
      const regionMap = {}
      this.allRegions.forEach(r => {
        if (r.regionId != null) {
          regionMap[r.regionId] = r.regionCode
        }
      })

      const promises = selectedRegionIds.map(regionId =>
        listServerByRegion(regionId).then(res => {
          const servers = res.data || []
          const regionCode = regionMap[regionId] || ''
          return servers.map(s => ({ serverId: s.serverId, serverName: s.serverName, regionCode }))
        })
      )
      Promise.all(promises).then(results => {
        this.allServers = results.flat()
        this.emitChange()
      })
    },

    /** 向父组件输出选中值 */
    emitChange() {
      this.$emit('input', {
        channelKeys: [...this.selectedChannels],
        serverIds: [...this.selectedServers]
      })
    },

    /** 重置所有选项 */
    resetAll() {
      this.selectedChannels = []
      this.selectedRegions = []
      this.selectedServers = []
      this.allRegions = []
      this.allServers = []
      this.emitChange()
    }
  }
}
</script>

<style scoped>
.css-root {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px 16px;
  background: #fafafa;
  display: flex;
  flex-direction: row;
  gap: 16px;
}
.css-item {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.css-item .el-select {
  flex: 1;
}
.css-label {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  white-space: nowrap;
  flex-shrink: 0;
}
</style>
