package com.trace.server.trace.service;

import com.trace.server.es.EsTemplate;
import com.trace.server.es.bean.AggEntity;
import com.trace.server.es.bean.Page;
import com.trace.server.es.bean.Result;
import com.trace.server.es.em.AggTypeEm;
import com.trace.server.es.em.TableEnum;
import com.trace.server.trace.dto.DateUtil;
import com.trace.server.trace.dto.RestResult;
import com.trace.server.trace.dto.SpanDTO;
import com.trace.server.trace.dto.TraceDTO;
import com.trace.server.trace.dto.req.TraceSpanReqDTO;
import com.trace.server.trace.dto.resp.MetricsRespDTO;
import com.trace.server.util.DoubleFormatUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月17日
 * @since 1.0
 */
@Service
public class TraceService {

    @Autowired
    private EsTemplate esTemplate;

    public RestResult<Object> queryListByTerm(TraceSpanReqDTO spanReqDTO) {

        Result<Page<SpanDTO>> result = esTemplate.queryListByTerm(TableEnum.TABLE_TRACE, spanReqDTO, SpanDTO.class);

        // 去重复
        List<SpanDTO> list = result.getData().getList().stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(SpanDTO::getId))), ArrayList::new));
        list.sort((a1, a2) -> (int) (a2.getStart() - a1.getStart()));

        // 处理开始时间
        for (SpanDTO spanDTO : list) {
            spanDTO.setStartTime(DateUtil.timeStamp2Date(spanDTO.getStart().toString(), "yyyy-MM-dd HH:mm:ss"));
        }

        result.getData().setList(list);
        result.getData().setTotalCount(list.size());
        return new RestResult<>().buildSuccess(result.getData(), queryMetrics(spanReqDTO).getData());
    }

    public RestResult queryByTraceId(TraceSpanReqDTO spanReqDTO) {
        Result<Page<SpanDTO>> result = esTemplate.queryListByTerm(TableEnum.TABLE_TRACE, spanReqDTO, SpanDTO.class);
        if (!result.isSuccess()) {
            return new RestResult().buildFail(result.getCode(), result.getMsg());
        }

        // 去重复
        List<SpanDTO> list = result.getData().getList().stream().collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(SpanDTO::getId))), ArrayList::new));
        list.sort((a1, a2) -> (int) (a1.getStart() - a2.getStart()));

        for (SpanDTO spanDTO : list) {
            spanDTO.setStartTime(DateUtil.timeStamp2Date(spanDTO.getStart().toString(), "yyyy-MM-dd HH:mm:ss"));
        }

        TraceDTO traceDTO = new TraceDTO();
        // 找到父节点
        for (SpanDTO spanDTO : list) {
            if ("0".equals(spanDTO.getParentId())) {
                BeanUtils.copyProperties(spanDTO, traceDTO);
            }
        }
        // 递归设置子节点
        this.setChildSpan(traceDTO, list);

        return new RestResult<>().buildSuccess(traceDTO);
    }

    /**
     * 寻找并添加子节点
     */
    private void setChildSpan(TraceDTO traceDTO, List<SpanDTO> spanList) {
        List<TraceDTO> childList = new ArrayList<>();

        // 寻找并添加子节点
        for (SpanDTO spanDTO : spanList) {
            if (traceDTO.getId().equals(spanDTO.getParentId())) {
                TraceDTO child = new TraceDTO();
                BeanUtils.copyProperties(spanDTO, child);
                childList.add(child);
                child.setStartTime(DateUtil.timeStamp2Date(child.getStart().toString(), "yyyy-MM-dd HH:mm:ss"));
            }
        }
        spanList.removeAll(childList);
        spanList.remove(traceDTO);
        traceDTO.setChildren(childList);

        // 递归调用
        for (TraceDTO parent : childList) {
            setChildSpan(parent, spanList);
        }

    }

    public RestResult<Object> queryMetrics(TraceSpanReqDTO spanReqDTO) {

        // 统计全部请求的最大、最小、平均调用时间、总调用次数
        spanReqDTO = spanReqDTO == null ? new TraceSpanReqDTO() : spanReqDTO;
        spanReqDTO.setResultType("");
        spanReqDTO.setStart(null);
        Map<String, AggEntity> map = new HashMap<>();
        map.put("max_duration", new AggEntity(AggTypeEm.MAX, "duration"));
        map.put("avg_duration", new AggEntity(AggTypeEm.AVG, "duration"));
        map.put("min_duration", new AggEntity(AggTypeEm.MIN, "duration"));
        map.put("count_all", new AggEntity(AggTypeEm.COUNT, "duration"));
        Map resultAll = esTemplate.agg(TableEnum.TABLE_TRACE, spanReqDTO, map);

        // 统计成功请求的调用次数
        Map<String, AggEntity> successMap = new HashMap<>();
        successMap.put("count_success", new AggEntity(AggTypeEm.COUNT, "resultType"));
        spanReqDTO.setResultType("success");
        Map resultSuccess = esTemplate.agg(TableEnum.TABLE_TRACE, spanReqDTO, successMap);

        // 封装返回值
        MetricsRespDTO metricsRespDTO = new MetricsRespDTO();
        metricsRespDTO.setAvgDuration(DoubleFormatUtil.formatToSeconds((Double) resultAll.get("avg_duration")));
        metricsRespDTO.setMaxDuration(DoubleFormatUtil.formatToSeconds((Double) resultAll.get("max_duration")));
        metricsRespDTO.setMinDuration(DoubleFormatUtil.formatToSeconds((Double) resultAll.get("min_duration")));
        metricsRespDTO.setTotalCount(((Double) resultAll.get("count_all")).intValue());

        // 计算错误率和错误数量
        int errorCount = (metricsRespDTO.getTotalCount() - ((Double) resultSuccess.get("count_success")).intValue());
        metricsRespDTO.setErrorCount(errorCount);
        metricsRespDTO.setErrorRate(DoubleFormatUtil.formatPercent(errorCount, metricsRespDTO.getTotalCount()));

        return new RestResult<>().buildSuccess(metricsRespDTO);
    }

    public RestResult<Object> queryServiceNames() {
        // 查询服务名和url列表
        Map<String, AggEntity> map = new HashMap<>();

        map.put("urls", new AggEntity(AggTypeEm.TERMS, "name.keyword", 50));
        map.put("serviceNames", new AggEntity(AggTypeEm.TERMS, "localServiceName.keyword", 50));

        Map result = esTemplate.agg(TableEnum.TABLE_TRACE, null, map);

        if (result.get("urls") != null && result.get("serviceNames") != null) {
            // 根据服务名排序
            Collections.sort((List) result.get("urls"));
            Collections.sort((List) result.get("serviceNames"));
        }

        return new RestResult<>().buildSuccess(result);
    }

}
