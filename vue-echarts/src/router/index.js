import Vue from 'vue';
import Router from 'vue-router';

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            component: resolve = > require(['../components/common/Home.vue'], resolve),
        meta
:
{
    title: '面板'
}
,
children:[
    {
        path: '/',
        component: resolve = > require(['../components/page/BaseForm.vue'], resolve),
    meta
:
{
    title: '条件检索'
}
},
{
    path: '/info',
        component
:
    resolve =
>
    require(['../components/page/InfoPannel.vue'], resolve),
        meta
:
    {
        title: '信息面板'
    }
}
]
}
]
})
