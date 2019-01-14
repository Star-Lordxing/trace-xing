<template>
    <div>
        <div class="container">
            <div class="form-box">
                <el-form ref="form" :model="form" :inline="true">
                    <el-form-item label="服务名">
                        <el-select v-model="form.name" placeholder="请选择服务名" >
                            <el-option
                              v-for="item in serviceNames"
                              :key="item"
                              :label="item"
                              :value="item">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="URL/接口">
                        <el-select v-model="form.url" placeholder="请选择URL/接口">
                            <el-option
                              v-for="item in urls"
                              :key="item"
                              :label="item"
                              :value="item">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="traceID">
                        <el-input v-model="form.trace" placeholder="请输入traceID"></el-input>
                    </el-form-item>
                    <el-form-item label="最近">
                        <el-select v-model="form.time" placeholder="请选择" >
                            <el-option label="一小时" value="1"></el-option>
                            <el-option label="一天" value="2"></el-option>
                            <el-option label="一周" value="3"></el-option>
                            <el-option label="一月" value="4"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="状态">
                        <el-select v-model="form.status" placeholder="请选择" >
                            <el-option label="success" value="success"></el-option>
                            <el-option label="exception" value="exception"></el-option>
                            <el-option label="error_result" value="error_result"></el-option>
                            <el-option label="timeout" value="timeout"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="submitForm()">搜索</el-button>
                        <el-button @click="resetForm()">重置</el-button>
                    </el-form-item>
                </el-form>
            </div>
            <div>
              <b>平均调用时间: </b>{{ result.avgDuration }}&nbsp;&nbsp;&nbsp;&nbsp;
              <b>最大调用时间: </b>{{ result.maxDuration }}&nbsp;&nbsp;&nbsp;&nbsp;
              <b>最小调用时间: </b>{{ result.minDuration }}&nbsp;&nbsp;&nbsp;&nbsp;
              <b>总调用次数: </b>{{ result.totalCount }}&nbsp;&nbsp;&nbsp;&nbsp;
              <b>错误次数: </b>{{ result.errorCount }}&nbsp;&nbsp;&nbsp;&nbsp;
              <b>错误率: </b>{{ result.errorRate }}&nbsp;&nbsp;&nbsp;&nbsp;
            </div>
            <el-table border class="table" :data="tableData" @current-change="handleCurrentChange" highlight-current-row>
              <el-table-column prop="traceId" label="traceId"></el-table-column>
                <el-table-column prop="localServiceName" label="服务名"></el-table-column>
                <el-table-column prop="name" label="URL" width="394"></el-table-column>
                <el-table-column prop="traceType" label="类型"></el-table-column>
                <el-table-column prop="startTime"  label="开始时间"></el-table-column>
                <el-table-column prop="duration" label="耗时"></el-table-column>
                <el-table-column prop="resultType" label="状态" align="center">  
	                <template slot-scope="scope">
                    <span v-if="scope.row.resultType === 'success'" style="color:green">{{ scope.row.resultType }}</span>
                    <span v-else style="color: red">{{ scope.row.resultType }}</span>
                  </template>
                </el-table-column>
            </el-table>
            <div class="pagination" v-show="false">
                <el-pagination background layout="prev, pager, next" :total="totalData">
                </el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
export default {
  name: "baseform",
  data() {
    return {
      totalData: 1,
      pageNo: 1,
      serviceNames: [],
      urls: [],
      form: {
        name: "",
        url: "",
        trace: "",
        time: "",
        status: ""
      },
      tableData: [],
      urlVal: this.$store.state.url,
      result: {}
    };
  },
  created() {
    this.initPage();
  },
  methods: {
    onSubmit() {
      this.$message.success("提交成功！");
    },
    initPage() {
      this.openFullScreen()
      this.$axios.post(this.urlVal + "/trace/queryServiceNames").then(res => {
        const data = res.data.data;
        this.serviceNames = data.serviceNames;
        this.urls = data.urls;
      });
      this.$axios
        .post(this.urlVal + "/trace/queryByString", {
          pageNo: this.pageNo
        })
        .then(res => {
          this.loading.close()
          
          this.tableData = res.data.data.list;
          this.result = res.data.data1;
        });
    },
    submitForm() {
      const time = new Date().getTime();
      let before;
      if (this.form.time === "1") {
        before = time - 60 * 60 * 1000;
      } else if (this.form.time === "2") {
        before = time - 24 * 60 * 60 * 1000;
      } else if (this.form.time === "3") {
        before = time - 24 * 60 * 60 * 1000 * 7;
      } else if (this.form.time === "4") {
        before = time - 24 * 60 * 60 * 1000 * 30;
      }

      this.openFullScreen()
      this.$axios
        .post(this.urlVal + "/trace/queryByString", {
          localServiceName: this.form.name,
          traceId: this.form.trace,
          start: this.form.time,
          resultType: this.form.status,
          pageNo: this.pageNo
        })
        .then(res => {
          this.loading.close()
          this.tableData = res.data.data.list;
          this.result = res.data.data1;
        });
    },
    resetForm() {
      this.form = {
        name: "",
        url: "",
        trace: "",
        time: "",
        status: ""
      };
    },
    timetrans(date) {
      var date = new Date(date); //如果date为13位不需要乘1000
      var Y = date.getFullYear() + "-";
      var M =
        (date.getMonth() + 1 < 10
          ? "0" + (date.getMonth() + 1)
          : date.getMonth() + 1) + "-";
      var D =
        (date.getDate() < 10 ? "0" + date.getDate() : date.getDate()) + " ";
      var h =
        (date.getHours() < 10 ? "0" + date.getHours() : date.getHours()) + ":";
      var m =
        (date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes()) +
        ":";
      var s =
        date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
      return Y + M + D + h + m + s;
    },
    handleCurrentChange(val) {
      console.log(val.traceId)
      var prom = new Promise((resolve, reject) => {
        resolve();
      })
      prom.then(() => {
        this.$store.commit("updateState", val.traceId);
      }).then((str) => {
        this.$router.push("/info");
      })
    },
    openFullScreen() {
      const loading = this.$loading({
        lock: true,
        text: "Loading",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)"
      });
      this.loading = loading
    }
  }
};
</script>