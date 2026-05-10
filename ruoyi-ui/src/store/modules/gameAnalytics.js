/**
 * 游戏分析模块 - 共享筛选状态
 * 在留存/LTV/充值等页面间保持 ServerSelector 选择一致
 */
const gameAnalytics = {
  namespaced: true,
  state: {
    serverSelection: { projectId: null, serverIds: [] },
    dateRange: []
  },
  mutations: {
    SET_SERVER_SELECTION(state, payload) {
      state.serverSelection = { projectId: payload.projectId, serverIds: (payload.serverIds || []).slice() }
    },
    SET_DATE_RANGE(state, range) {
      state.dateRange = range ? [...range] : []
    },
    RESET(state) {
      state.serverSelection = { projectId: null, serverIds: [] }
      state.dateRange = []
    }
  }
}
export default gameAnalytics
