<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属实体" prop="entityType">
        <el-select v-model="queryParams.entityType" placeholder="所属实体" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.game_entity_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="字段名称" prop="fieldLabel">
        <el-input
          v-model="queryParams.fieldLabel"
          placeholder="请输入字段名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="字段编码" prop="fieldCode">
        <el-input
          v-model="queryParams.fieldCode"
          placeholder="请输入字段编码"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 240px">
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['game:field:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['game:field:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['game:field:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['game:field:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="fieldDefineList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="字段ID" align="center" prop="fieldId" />
      <el-table-column label="所属实体" align="center" prop="entityType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.game_entity_type" :value="scope.row.entityType"/>
        </template>
      </el-table-column>
      <el-table-column label="字段名称" align="center" prop="fieldLabel" :show-overflow-tooltip="true" />
      <el-table-column label="字段编码" align="center" prop="fieldCode" :show-overflow-tooltip="true" />
      <el-table-column label="字段类型" align="center" prop="fieldType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.game_field_type" :value="scope.row.fieldType"/>
        </template>
      </el-table-column>
      <el-table-column label="是否导出" align="center" prop="isExport">
        <template slot-scope="scope">
          <span>{{ scope.row.isExport === 'Y' ? '是' : '否' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序号" align="center" prop="sort" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['game:field:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['game:field:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改字段定义对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属实体" prop="entityType">
          <el-select v-model="form.entityType" placeholder="请选择所属实体" style="width: 100%">
            <el-option
              v-for="dict in dict.type.game_entity_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="字段名称" prop="fieldLabel">
          <el-input v-model="form.fieldLabel" placeholder="请输入字段名称" />
        </el-form-item>
        <el-form-item label="字段编码" prop="fieldCode">
          <el-input v-model="form.fieldCode" placeholder="请输入字段编码(小写字母, 连字符分隔)" />
        </el-form-item>
        <el-form-item label="字段类型" prop="fieldType">
          <el-select v-model="form.fieldType" placeholder="请选择字段类型" style="width: 100%" @change="handleFieldTypeChange">
            <el-option
              v-for="dict in dict.type.game_field_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.fieldType === 'select'" label="选项配置" prop="fieldOptions">
          <el-input
            v-model="form.fieldOptions"
            type="textarea"
            :rows="4"
            placeholder='JSON格式: [{"label":"选项1","value":"1"},{"label":"选项2","value":"2"}]'
          />
        </el-form-item>
        <el-form-item label="是否导出" prop="isExport">
          <el-radio-group v-model="form.isExport">
            <el-radio label="Y">是</el-radio>
            <el-radio label="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否必填" prop="isRequired">
          <el-radio-group v-model="form.isRequired">
            <el-radio label="Y">是</el-radio>
            <el-radio label="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序号" prop="sort">
          <el-input-number v-model="form.sort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listFieldDefine, getFieldDefine, delFieldDefine, addFieldDefine, updateFieldDefine } from "@/api/game/fieldDefine"

export default {
  name: "FieldDefine",
  dicts: ['sys_normal_disable', 'game_entity_type', 'game_field_type'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 字段定义表格数据
      fieldDefineList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        entityType: undefined,
        fieldLabel: undefined,
        fieldCode: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        entityType: [
          { required: true, message: "所属实体不能为空", trigger: "change" }
        ],
        fieldLabel: [
          { required: true, message: "字段名称不能为空", trigger: "blur" }
        ],
        fieldCode: [
          { required: true, message: "字段编码不能为空", trigger: "blur" }
        ],
        fieldType: [
          { required: true, message: "字段类型不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询字段定义列表 */
    getList() {
      this.loading = true
      listFieldDefine(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.fieldDefineList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 字段类型变更处理 */
    handleFieldTypeChange(val) {
      if (val !== 'select') {
        this.form.fieldOptions = undefined
      }
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        fieldId: undefined,
        entityType: undefined,
        fieldLabel: undefined,
        fieldCode: undefined,
        fieldType: undefined,
        fieldOptions: undefined,
        isExport: "Y",
        isRequired: "N",
        sort: undefined,
        status: "0",
        remark: undefined
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加字段定义"
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.fieldId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const fieldId = row.fieldId || this.ids
      getFieldDefine(fieldId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改字段定义"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.fieldId != undefined) {
            updateFieldDefine(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addFieldDefine(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const fieldIds = row.fieldId || this.ids
      this.$modal.confirm('是否确认删除字段定义编号为"' + fieldIds + '"的数据项？').then(function() {
        return delFieldDefine(fieldIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('/system/game/field/define/export', {
        ...this.queryParams
      }, `field_define_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
