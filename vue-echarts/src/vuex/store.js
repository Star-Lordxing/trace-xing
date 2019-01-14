import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex)
const store = new Vuex.Store({
    state: {
        count: 0,
        traceId: '',
        url: 'http://172.22.51.117:8082'
    },
    mutations: {
        updateState(state, traceId) {
            state.traceId = traceId
            return '/info'
        }
    }
})
export default store
