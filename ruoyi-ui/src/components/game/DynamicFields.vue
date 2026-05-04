<template>
  <div v-if="fieldDefines && fieldDefines.length > 0">
    <template v-if="formType === 'form'">
      <el-form-item
        v-for="field in fieldDefines"
        :key="field.fieldCode"
        :label="field.fieldLabel"
        :required="field.isRequired === 'Y'"
        :prop="'dynamicFields.' + field.fieldCode"
      >
        <!-- 文本输入 -->
        <el-input
          v-if="field.fieldType === 'text'"
          v-model="dynamicValues[field.fieldCode]"
          :placeholder="'请输入' + field.fieldLabel"
          clearable
        />
        <!-- 数字输入 -->
        <el-input-number
          v-else-if="field.fieldType === 'number'"
          v-model="dynamicValues[field.fieldCode]"
          :step="1"
          controls-position="right"
          style="width: 100%"
        />
        <!-- 文本域 -->
        <el-input
          v-else-if="field.fieldType === 'textarea'"
          v-model="dynamicValues[field.fieldCode]"
          type="textarea"
          :placeholder="'请输入' + field.fieldLabel"
          :rows="3"
        />
        <!-- 日期时间 -->
        <el-date-picker
          v-else-if="field.fieldType === 'date'"
          v-model="dynamicValues[field.fieldCode]"
          type="datetime"
          :placeholder="'请选择' + field.fieldLabel"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="width: 100%"
        />
        <!-- 下拉选择 -->
        <el-select
          v-else-if="field.fieldType === 'select'"
          v-model="dynamicValues[field.fieldCode]"
          :placeholder="'请选择' + field.fieldLabel"
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="opt in parseFieldOptions(field.fieldOptions)"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <!-- 布尔开关 -->
        <el-switch
          v-else-if="field.fieldType === 'boolean'"
          v-model="dynamicValues[field.fieldCode]"
          :active-value="boolActiveValue"
          :inactive-value="boolInactiveValue"
        />
        <!-- 默认文本输入 -->
        <el-input
          v-else
          v-model="dynamicValues[field.fieldCode]"
          :placeholder="'请输入' + field.fieldLabel"
          clearable
        />
      </el-form-item>
    </template>
    <template v-else>
      <template v-for="field in fieldDefines">
        <el-table-column
          v-if="field.fieldType === 'boolean'"
          :key="field.fieldCode"
          :label="field.fieldLabel"
          align="center"
          :prop="'dynamicFields.' + field.fieldCode"
        >
          <template slot-scope="scope">
            <dict-tag
              v-if="scope.row.dynamicFields"
              :options="[{ label: '是', value: 'Y' }, { label: '否', value: 'N' }]"
              :value="scope.row.dynamicFields[field.fieldCode]"
            />
          </template>
        </el-table-column>
        <el-table-column
          v-else
          :key="field.fieldCode"
          :label="field.fieldLabel"
          align="center"
          :prop="'dynamicFields.' + field.fieldCode"
          :show-overflow-tooltip="true"
        />
      </template>
    </template>
  </div>
</template>

<script>
export default {
  name: 'DynamicFields',
  props: {
    fieldDefines: {
      type: Array,
      default: () => []
    },
    value: {
      type: Object,
      default: () => ({})
    },
    formType: {
      type: String,
      default: 'form'
    }
  },
  computed: {
    dynamicValues: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    },
    boolActiveValue() {
      return 'Y'
    },
    boolInactiveValue() {
      return 'N'
    }
  },
  watch: {
    dynamicValues: {
      handler(val) {
        this.$emit('input', val)
      },
      deep: true
    }
  },
  methods: {
    parseFieldOptions(optionsStr) {
      if (!optionsStr) return []
      try {
        const parsed = JSON.parse(optionsStr)
        return Array.isArray(parsed) ? parsed : []
      } catch (e) {
        return []
      }
    }
  }
}
</script>
