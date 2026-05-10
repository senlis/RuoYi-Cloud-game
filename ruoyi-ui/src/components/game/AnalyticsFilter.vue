<template>
  <div>
    <ServerSelector v-model="selection" @query="onQuery" />
    <div class="af-extra" v-if="$slots.extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script>
import ServerSelector from "@/components/game/ServerSelector";

export default {
  name: "AnalyticsFilter",
  components: { ServerSelector },
  props: { value: { type: Object, default: () => ({}) } },
  data() {
    return { selection: { projectId: null, serverIds: [] } };
  },
  methods: {
    onQuery(sel, daterange) {
      this.selection = sel;
      this.$emit("query", sel, daterange);
    },
    getRange() {
      // 从子组件获取日期范围
      return this.$el.querySelector('.el-date-picker') ? [] : [];
    }
  }
};
</script>
