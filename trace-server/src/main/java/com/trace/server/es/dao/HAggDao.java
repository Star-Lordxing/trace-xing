package com.trace.server.es.dao;

import com.trace.server.es.bean.AggEntity;
import com.trace.server.es.config.InitClient;
import com.trace.server.es.em.AggTypeEm;
import com.trace.server.es.em.TableEnum;
import com.trace.server.trace.dto.req.TraceSpanReqDTO;
import com.trace.server.util.ParamUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HAggDao {

    private static Logger logger = LogManager.getRootLogger();

    public static <T> Map<String, Object> agg(TableEnum table, T param, Map<String, AggEntity> map) {
        Map<String, Object> result = new HashMap<>();

        try (RestHighLevelClient client = InitClient.getHighClient();) {

            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(table.getIndex());
            searchRequest.types(table.getType());

            // 2、复合查询条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder mustQuery = ParamUtil.buildParam(param);

            // 时间
            if (param instanceof TraceSpanReqDTO) {
                TraceSpanReqDTO spanReqDTO = (TraceSpanReqDTO) param;
                RangeQueryBuilder range = new RangeQueryBuilder("start")
                        .from(TraceSpanReqDTO.getStartDate(spanReqDTO));
                mustQuery.must(range);
            }

            if (mustQuery.hasClauses()) {
                sourceBuilder.query(mustQuery);
            }

            // 3、添加聚合
            for (Map.Entry<String, AggEntity> entry : map.entrySet()) {
                if (AggTypeEm.AVG.equals(entry.getValue().getTypeEm())) {
                    sourceBuilder.aggregation(
                            AggregationBuilders.avg(entry.getKey()).field(entry.getValue().getField())
                    );
                } else if (AggTypeEm.COUNT.equals(entry.getValue().getTypeEm())) {
                    sourceBuilder.aggregation(
                            AggregationBuilders.count(entry.getKey()).field(entry.getValue().getField())
                    );
                } else if (AggTypeEm.SUM.equals(entry.getValue().getTypeEm())) {
                    sourceBuilder.aggregation(
                            AggregationBuilders.sum(entry.getKey()).field(entry.getValue().getField())
                    );
                } else if (AggTypeEm.MIN.equals(entry.getValue().getTypeEm())) {
                    sourceBuilder.aggregation(
                            AggregationBuilders.min(entry.getKey()).field(entry.getValue().getField())
                    );
                } else if (AggTypeEm.MAX.equals(entry.getValue().getTypeEm())) {
                    sourceBuilder.aggregation(
                            AggregationBuilders.max(entry.getKey()).field(entry.getValue().getField())
                    );
                } else if (AggTypeEm.TERMS.equals(entry.getValue().getTypeEm())) {
                    sourceBuilder.aggregation(
                            AggregationBuilders.terms(entry.getKey()).field(entry.getValue().getField())
                    );
                }
            }

            //4、 执行请求
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);

            //5、处理响应
            //搜索结果状态信息
            if (RestStatus.OK.equals(searchResponse.status())) {
                // 获取聚合结果
                Aggregations aggregations = searchResponse.getAggregations();

                for (Map.Entry<String, AggEntity> entry : map.entrySet()) {
                    if (AggTypeEm.TERMS.equals(entry.getValue().getTypeEm())) {
                        List<Object> termList = new ArrayList<>();
                        Terms terms = aggregations.get(entry.getKey());
                        for (Terms.Bucket buck :  terms.getBuckets()) {
                            termList.add(buck.getKey());
                        }
                        result.put(entry.getKey(), termList);
                    } else {
                        NumericMetricsAggregation.SingleValue value = aggregations.get(entry.getKey());
                        result.put(entry.getKey(), value.value());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }

        return result;
    }
}