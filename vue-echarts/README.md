# vue-echarts
vue-echarts

## 本地开发环境
```shell
<!-- 开发环境 -->
Node.js v8.9.1
<!-- 安装依赖 -->
$ npm install -S
<!-- 运行 -->
$ npm run dev
```

## 接口地址修改
```js
// src/vuex/store.js 文件
const store = new Vuex.Store({
    state: {
        count: 0,
        traceId: '',
        url: 'http://localhost:8090'
    },
    mutations: {
        updateState(state, traceId) {
            state.traceId = traceId
        }
    }
})
// 修改 url 的指向，注意地址末尾不要带 '/'
```

## 生产
```shell
npm run build
```
程序运行结束后生成 `dist` 文件夹，该文件夹目录下为静态资源
