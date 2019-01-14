<template>
  <div class="container">
    <div id="orgManagement"></div>
    <el-dialog :title="clickData.localServiceName" :visible.sync="dialogStatus" width="70%">
        <div class="model-tabel">
          <h3>基本信息</h3>
          <el-table :data="tableData">
            <el-table-column prop="traceId" label="traceId"></el-table-column>
            <el-table-column prop="parentId" label="parentId"></el-table-column>
            <el-table-column prop="id" label="id"></el-table-column>
            <el-table-column prop="name" label="name"></el-table-column>
            <el-table-column prop="traceType" label="traceType"></el-table-column>
            <el-table-column prop="resultType" label="resultType"></el-table-column>
            <el-table-column prop="start" label="start"></el-table-column>
            <el-table-column prop="duration" label="duration"></el-table-column>
          </el-table>
        </div>
        <div class="model-tabel">
          <h3>本地信息</h3>
          <el-table :data="tableData">
            <el-table-column prop="localServiceName" label="localServiceName"></el-table-column>
            <el-table-column prop="localHost" label="localHost"></el-table-column>
            <el-table-column prop="localPort" label="localPort"></el-table-column>
          </el-table>
        </div>
        <div class="model-tabel">
          <h3>远程信息</h3>
          <el-table :data="tableData">
            <el-table-column prop="remoteServiceName" label="remoteServiceName"></el-table-column>
            <el-table-column prop="remoteHost" label="remoteHost"></el-table-column>
            <el-table-column prop="remotePort" label="remotePort"></el-table-column>
          </el-table>
        </div>
        <h4>tagMap:</h4>
        <p>{{ tagMap }}</p>
        <h4>exceptionType:</h4>
        <p>{{ exceptionType }}</p>
        <h4>exceptionMsg:</h4>
        <p>{{ exceptionMsg }}</p>
    </el-dialog>
  </div>
</template>

<script>
import echarts from "echarts";

export default {
  data() {
    return {
      chartData: {
        name: "父节点",
        children: [
          {
            name: "子节点",
            children: [
              {
                name: "子节点1",
                children: [
                  { name: "子1子节点1", value: 3938 },
                  { name: "子1子节点2", value: 3812 }
                ]
              },
              {
                name: "子节点2",
                children: [
                  {
                    name: "子2子节点1",
                    children: [
                      { name: "子2子节点1", value: 3938 },
                      { name: "子2子节点2", value: 3812 }
                    ]
                  },
                  { name: "子2子节点2", value: 3812 }
                ]
              }
            ]
          }
        ]
      },
      traceId: this.$store.state.traceId,
      dialogStatus: false,
      clickData: {},
      tableData: [],
      urlVal: this.$store.state.url,
      exceptionType: "",
      exceptionMsg: "",
      tagMap: "",
      colorCtl: 'green'
    };
  },
  methods: {
    initPage() {
      console.log(this.traceId)
      this.$axios
        .post(this.urlVal + "/trace/queryByTraceId", {
          traceId: this.traceId
        })
        .then(res => {
          console.log(res)
          const data = res.data.data;
          this.chartData = data;
          this.initChart();
        });
    },
    initChart() {
      const self = this;
      this.chart = echarts.init(document.getElementById("orgManagement"));
      this.chart.on("contextmenu", params => {
        if (params.componentType === "series") {
          this.selectedOrg = params.data;
          this.popoverPanelShow = true;
        } else {
          return;
        }
      });
      this.chart.on("click", params => {
        this.tableData = [];
        this.dialogStatus = true;
        this.clickData = params.data;
        this.tagMap = JSON.stringify(params.data.tagMap, null, 2);
        this.exceptionType = params.data.exceptionType;
        this.exceptionMsg = params.data.exceptionMsg;
        this.tableData.push(params.data);
      });
      this.chart.setOption({
        series: [
          {
            type: "tree",

            data: [this.chartData],

            top: "0%",
            left: "10%",
            bottom: "0%",
            right: "10%",
            symbolSize: 120,
            color: "#000",
            
            itemStyle: {
              normal: {
                borderColor: self.colorCtl
              }
            },

            label: {
              normal: {
                formatter: value => {
                  let data = value.data;
                  // console.log(data)
                  if (data.resultType !== 'success') {
                    // data.itemStyle.normal.borderColor = 'green'
                  }
                  let name = data.name;
                  if (name[0] === "h") {
                    let res = [];
                    let arr = name.split("/");
                    for (let i = 3; i < arr.length; i++) {
                      res.push(arr[i]);
                    }
                    name = "/" + res.join("/");
                  } else {
                    let arr = name.split("#");
                    let tmp = arr[0].split(".");

                    name = tmp[tmp.length - 1] + "#" + arr[1];
                  }

                  const localServiceName = data.localServiceName;
                  const traceType = data.traceType;
                  const duration = data.duration;
                  const result =
                    localServiceName +
                    "\n" +
                    name +
                    "\n" +
                    traceType +
                    "\n" +
                    duration +
                    "ms";

                  return result;
                },
                position: "inside",
                verticalAlign: "middle",
                align: "center",
                fontSize: 12
              }
            },

            expandAndCollapse: false,
            animationDuration: 550,
            animationDurationUpdate: 750
          }
        ]
      });
    },
    hidePopoverPanel() {
      this.popoverPanelShow = false;
    }
  },
  mounted() {
    this.initPage();
  },
  beforeDestroy() {
    if (!this.chart) {
      return;
    }
    this.chart.dispose();
    this.chart = null;
  }
};
</script>

<style>
.vis-data {
  height: 700px;
}
.node-parent {
  height: 150px;
  width: 150px;
  background-color: #409eff;
  border-radius: 50%;
  margin: auto 0;
  float: left;
  position: relative;
}
.el-tree-node__expand-icon {
  display: none !important;
}
</style>
