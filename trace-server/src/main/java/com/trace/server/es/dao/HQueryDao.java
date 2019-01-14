package com.trace.server.es.dao;

import com.alibaba.fastjson.JSON;
import com.trace.server.es.bean.Page;
import com.trace.server.es.bean.Result;
import com.trace.server.es.config.InitClient;
import com.trace.server.es.config.ResultConstant;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月14日
 * @since 1.0
 */
public class HQueryDao {
    private static Logger logger = LogManager.getRootLogger();

    public static <T> Result<Page<T>> queryListByTerm(TableEnum table, Page param, Class<?> respClass) {

        List<T> dataList = new ArrayList<>();
        Page<T> page = new Page<T>();
        if (param != null) {
            BeanUtils.copyProperties(param, page);
        }

        try (RestHighLevelClient client = InitClient.getHighClient();) {

            // 1、创建search请求
            SearchRequest searchRequest = new SearchRequest(table.getIndex());
            searchRequest.types(table.getType());

            // 2、复合查询条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            BoolQueryBuilder mustQuery = ParamUtil.buildParam(param);

            // 时间
            if (param instanceof TraceSpanReqDTO && ((TraceSpanReqDTO) param).getStart() != null) {
                TraceSpanReqDTO spanReqDTO = (TraceSpanReqDTO) param;
                RangeQueryBuilder range = new RangeQueryBuilder("start")
                        .from(TraceSpanReqDTO.getStartDate(spanReqDTO).getTime()).to(System.currentTimeMillis());
                mustQuery.must(range);
            }

            if (mustQuery.hasClauses()) {
                sourceBuilder.query(mustQuery);
            }

            // 3、设置分页
            if (param != null) {
                sourceBuilder.from(param.getPageNo() != null ? param.getPageNo() - 1 : 0);
                sourceBuilder.size(param.getPageSize() == null ? 200 : param.getPageSize());
            }

            sourceBuilder.sort("start", SortOrder.DESC);

            //4、 执行请求
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);

            //5、处理响应
            SearchHits hits = searchResponse.getHits();
            // 总记录数
            page.setTotalCount((int) hits.getTotalHits());
            // 命中记录
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                //取_source字段值
                String sourceAsString = hit.getSourceAsString();          // 取成json串
                T obj = (T) JSON.parseObject(sourceAsString, respClass);   // 转换对象
                dataList.add(obj);
            }

            page.setList(dataList);

        } catch (IOException e) {
            logger.error(e);
            new Result().buildFail(ResultConstant.QUERY_ERROR_CODE, ResultConstant.QUERY_ERROR_MSG);
        }

        return new Result().buildSuccess(page);
    }
}
