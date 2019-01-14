package com.trace.server.kafka;

import com.alibaba.fastjson.JSON;
import com.trace.server.es.EsTemplate;
import com.trace.server.es.em.TableEnum;
import com.trace.server.trace.dto.TraceDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {


    @Autowired
    EsTemplate esTemplate;

    @KafkaListener(topics = "finance_trace")
    public void listenT1(ConsumerRecord<?, ?> cr) throws Exception {
        TraceDTO traceDTO = JSON.parseObject(cr.value().toString(), TraceDTO.class);
        esTemplate.insert(TableEnum.TABLE_TRACE, traceDTO);
    }
}
