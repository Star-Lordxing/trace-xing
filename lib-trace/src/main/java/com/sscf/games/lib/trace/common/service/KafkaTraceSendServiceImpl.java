package com.sscf.games.lib.trace.common.service;

import com.sscf.games.lib.trace.common.TraceSpan;
import com.sscf.games.lib.trace.common.dto.TraceSpanDTO;
import com.sscf.games.lib.trace.common.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Service
@Slf4j
public class KafkaTraceSendServiceImpl implements TraceSendService {

    private static final String TRACE_TOPIC = "finance_trace";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void send(TraceSpan traceSpan) {
        TraceSpanDTO spanDTO = this.buildSpanDTO(traceSpan);
        String value = ObjectUtil.toJsonStr(spanDTO);
        kafkaTemplate.send(TRACE_TOPIC, spanDTO.getTraceId() + spanDTO.getId(), value);
        log.info(value);
    }

    private TraceSpanDTO buildSpanDTO(TraceSpan traceSpan) {
        TraceSpanDTO spanDTO = new TraceSpanDTO();
        spanDTO.setTraceId(traceSpan.getTraceId());
        spanDTO.setId(traceSpan.getId());
        spanDTO.setParentId(traceSpan.getParentId());
        spanDTO.setName(traceSpan.getName());
        spanDTO.setTraceType(traceSpan.getTraceTypeEnum().getName());
        spanDTO.setResultType(traceSpan.getResultTypeEnum().getName());
        spanDTO.setLocalServiceName(traceSpan.getLocalPoint().getServiceName());
        spanDTO.setLocalHost(traceSpan.getLocalPoint().getHost());
        spanDTO.setLocalPort(traceSpan.getLocalPoint().getPort());

        spanDTO.setRemoteServiceName(traceSpan.getRemotePoint().getServiceName());
        spanDTO.setRemoteHost(traceSpan.getRemotePoint().getHost());
        spanDTO.setRemotePort(traceSpan.getRemotePoint().getPort());

        spanDTO.setStart(traceSpan.getStart());
        spanDTO.setDuration(traceSpan.getDuration());
        if (traceSpan.getException() != null) {
            spanDTO.setExceptionType(traceSpan.getException().getClass().getName());
            spanDTO.setExceptionMsg(traceSpan.getExceptionMsg());
        }
        spanDTO.setTagMap(traceSpan.getTagMap());
        return spanDTO;
    }
}