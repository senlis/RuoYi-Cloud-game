<template>
  <div class="item-selector">
    <div class="is-search">
      <el-input v-model="searchKeyword" placeholder="搜索道具ID或名称" size="small" clearable
        prefix-icon="el-icon-search" style="width:100%" @input="onSearch" />
    </div>

    <div class="is-list" v-loading="loading">
      <div v-if="searchResults.length === 0 && !loading" class="is-empty">输入关键字搜索道具</div>
      <div v-for="item in searchResults" :key="item.itemId" class="is-item">
        <el-checkbox v-model="selectedMap[item.itemId]" @change="val => onSelect(item, val)">
          <span class="is-id">{{ item.itemId }}</span>
          <span class="is-name">{{ item.itemName }}</span>
        </el-checkbox>
      </div>
    </div>

    <div v-if="selectedItems.length > 0" class="is-selected">
      <div class="is-selected-title">已选道具（{{ selectedItems.length }}）</div>
      <div v-for="(row, idx) in selectedItems" :key="row.itemId" class="is-selected-row">
        <span class="is-selected-info">{{ row.itemId }} - {{ row.itemName }}</span>
        <el-input-number v-model="row.count" :min="1" :max="99999999" size="small" style="width:130px" />
        <el-button type="danger" icon="el-icon-delete" size="mini" circle @click="removeItem(idx)" />
      </div>
    </div>
  </div>
</template>

<script>
import { listItem } from "@/api/game/item"

export default {
  name: "ItemSelector",
  props: {
    value: { type: Array, default: () => [] },
    projectId: { type: Number, default: null }
  },
  data() {
    return {
      searchKeyword: "",
      loading: false,
      searchResults: [],
      allItems: [],
      // 选中项的映射 { itemId: true/false }
      selectedMap: {}
    }
  },
  computed: {
    // 选中列表（含数量）
    selectedItems: {
      get() {
        return this.value || []
      },
      set(val) {
        this.$emit('input', val)
      }
    }
  },
  methods: {
    onSearch() {
      if (!this.searchKeyword) {
        this.searchResults = []
        return
      }
      this.loading = true
      // 从道具表中搜索
      listItem({ itemName: this.searchKeyword, projectId: this.projectId }).then(res => {
        this.searchResults = (res.rows || []).slice(0, 50)
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    onSelect(item, checked) {
      let list = [...this.selectedItems]
      if (checked) {
        list.push({ itemId: item.itemId, itemName: item.itemName, count: 1 })
      } else {
        list = list.filter(r => r.itemId !== item.itemId)
      }
      this.selectedItems = list
    },
    removeItem(idx) {
      const list = [...this.selectedItems]
      const removed = list.splice(idx, 1)
      if (removed.length > 0) {
        this.selectedMap[removed[0].itemId] = false
      }
      this.selectedItems = list
    }
  },
  watch: {
    value: {
      immediate: true,
      handler(val) {
        if (val && val.length > 0) {
          const map = {}
          val.forEach(r => { map[r.itemId] = true })
          this.selectedMap = map
        } else {
          this.selectedMap = {}
          this.searchKeyword = ""
          this.searchResults = []
        }
      }
    }
  }
}
</script>

<style scoped>
.item-selector {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
}
.is-search { margin-bottom: 8px; }
.is-list { max-height: 200px; overflow-y: auto; margin-bottom: 8px; }
.is-empty { color: #c0c4cc; text-align: center; padding: 20px 0; font-size: 13px; }
.is-item { padding: 4px 0; }
.is-item .is-id { color: #409eff; margin-right: 8px; font-weight: bold; }
.is-item .is-name { color: #606266; }
.is-selected { border-top: 1px solid #ebeef5; padding-top: 8px; }
.is-selected-title { font-size: 13px; font-weight: bold; color: #606266; margin-bottom: 6px; }
.is-selected-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.is-selected-info { flex: 1; font-size: 13px; }
</style>
