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
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月14日
 * @since 1.0
 */
public class QueryDao {
    private static Logger logger = LogManager.getRootLogger();

    public static <T> Result<Page<T>> queryListByTerm(TableEnum table, Page param, Class<?> respClass) {

        List<T> dataList = new ArrayList<>();
        Page page = new Page();
        if (param != null) {
            BeanUtils.copyProperties(param, page);
        }

        try (TransportClient client = InitClient.getClient();) {

            // 1、创建search请求
            SearchRequestBuilder searchRequest = client.prepareSearch(table.getIndex());

            // 2、复合查询条件
            BoolQueryBuilder mustQuery = ParamUtil.buildParam(param);
            if (mustQuery.hasClauses()) {
                searchRequest.setQuery(mustQuery);
            }

            // 时间
            if (param instanceof TraceSpanReqDTO && ((TraceSpanReqDTO) param).getStart() != null) {
                TraceSpanReqDTO spanReqDTO = (TraceSpanReqDTO) param;
                RangeQueryBuilder range = new RangeQueryBuilder("start")
                        .from(TraceSpanReqDTO.getStartDate(spanReqDTO).getTime()).to(System.currentTimeMillis());

                mustQuery.must(range);
            }

            // 3、设置分页
            if (param != null) {
                searchRequest.setFrom(param.getPageNo() != null ? param.getPageNo() - 1 : 0);
                searchRequest.setSize(param.getPageSize() == null ? 200 : param.getPageSize());
            }

            searchRequest.addSort("start", SortOrder.DESC);

            //4、 执行请求
            SearchResponse searchResponse = searchRequest.execute().actionGet();

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

        } catch (UnknownHostException e) {
            logger.error(e);
            new Result().buildFail(ResultConstant.QUERY_ERROR_CODE, ResultConstant.QUERY_ERROR_MSG);
        }

        return new Result().buildSuccess(page);
    }
}
