package com.trace.server.trace.controller;

import com.trace.server.trace.dto.RestResult;
import com.trace.server.trace.dto.req.TraceSpanReqDTO;
import com.trace.server.trace.service.TraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/trace")
public class TraceController {

    @Autowired
    private TraceService traceService;

    /**
     * 查询调用链列表
     */
    @RequestMapping(value = "/queryByString")
    public RestResult queryListByTerm(@RequestBody(required = false) TraceSpanReqDTO spanReqDTO) {
        return traceService.queryListByTerm(spanReqDTO);
    }

    /**
     * 根据traceId查询调用链
     */
    @RequestMapping(value = "/queryByTraceId")
    public RestResult queryByTraceId(@RequestBody(required = false) TraceSpanReqDTO spanReqDTO) {
        return traceService.queryByTraceId(spanReqDTO);
    }

    /**
     * 查询统计信息
     */
    @RequestMapping(value = "/queryMetrics")
    public RestResult queryMetrics(@RequestBody(required = false) TraceSpanReqDTO spanReqDTO) {
        return traceService.queryMetrics(spanReqDTO);
    }

    /**
     * 查询服务名和接口名
     */
    @RequestMapping(value = "/queryServiceNames")
    public RestResult queryServiceNames() {
        return traceService.queryServiceNames();
    }

}
