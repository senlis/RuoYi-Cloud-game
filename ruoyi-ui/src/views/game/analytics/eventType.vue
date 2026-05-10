<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="事件类型" prop="eventType">
        <el-input v-model="queryParams.eventType" placeholder="请输入事件类型编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="事件名称" prop="eventName">
        <el-input v-model="queryParams.eventName" placeholder="请输入事件名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['game:analytics:eventType']">新增</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="typeList">
      <el-table-column label="事件类型" align="center" prop="eventType" width="180" />
      <el-table-column label="事件名称" align="center" prop="eventName" width="180" />
      <el-table-column label="参数定义" align="center" prop="paramDefine" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 编辑对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="事件类型" prop="eventType">
          <el-input v-model="form.eventType" placeholder="如 pass" :disabled="form.id != null" />
        </el-form-item>
        <el-form-item label="事件名称" prop="eventName">
          <el-input v-model="form.eventName" placeholder="如 通关" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="事件触发时机说明" />
        </el-form-item>

        <el-divider content-position="left">参数解析定义（JSON）</el-divider>
        <el-form-item label="参数定义" prop="paramDefine">
          <el-input
            v-model="form.paramDefine"
            type="textarea"
            :rows="10"
            placeholder='{
  "n1": {"label": "关卡ID", "type": "number"},
  "n2": {"label": "难度", "type": "number"},
  "n3": {"label": "是否胜利", "type": "enum", "enumValues": {"0": "失败", "1": "胜利"}}
}'
          />
        </el-form-item>
        <el-alert title="参数说明" type="info" :closable="false" show-icon>
          <template slot="title">
            <span>type 支持: number / string / enum。enum 类型需提供 enumValues 映射。</span>
          </template>
        </el-alert>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listEventType, getEventType, addEventType, updateEventType, delEventType } from "@/api/game/analytics";

export default {
  name: "EventTypeConfig",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: true,
      total: 0,
      typeList: [],
      showSearch: true,
      open: false,
      title: "",
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        eventType: undefined,
        eventName: undefined
      },
      form: {},
      rules: {
        eventType: [
          { required: true, message: "事件类型编码不能为空", trigger: "blur" }
        ],
        eventName: [
          { required: true, message: "事件名称不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listEventType(this.queryParams).then(response => {
        this.typeList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        id: undefined,
        eventType: undefined,
        eventName: undefined,
        status: "0",
        remark: undefined,
        paramDefine: undefined
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增事件类型";
    },
    handleUpdate(row) {
      this.reset();
      getEventType(row.id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "编辑事件类型";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateEventType(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addEventType(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除事件类型 "' + row.eventType + '"？').then(() => {
        return delEventType(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
