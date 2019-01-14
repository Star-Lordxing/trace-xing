package com.trace.server.es.dao;

import com.alibaba.fastjson.JSON;
import com.trace.server.es.bean.Result;
import com.trace.server.es.config.InitClient;
import com.trace.server.es.config.ResultConstant;
import com.trace.server.es.em.TableEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月14日
 * @since 1.0
 */
@Service
public class InsertDao {
    private static Logger logger = LogManager.getRootLogger();

    public static <T> Result insert(TableEnum table, T param) {
        // 1、创建索引请求
        String index = table.getIndex();
        IndexRequest request = new IndexRequest(index, table.getType());
        // 2、准备文档数据,json串
        request.source(JSON.toJSONString(param), XContentType.JSON);

        // 4、发送请求
        IndexResponse indexResponse = null;

        try {
            indexResponse = InitClient.getClient().index(request).get();
        } catch (ElasticsearchException e) {
            // 判断是否版本冲突、create但文档已存在冲突
            if (e.status() == RestStatus.CONFLICT) {
                logger.error("版本号冲突" + e.getDetailedMessage());
            }
            logger.error("索引异常", e);
        } catch (InterruptedException | ExecutionException | UnknownHostException e) {
            logger.error("索引异常", e);
        }

        //5、处理响应
        if (indexResponse != null) {
            Result result = new Result<>(indexResponse.getIndex(),
                    indexResponse.getType(),
                    indexResponse.getId(),
                    indexResponse.getVersion()
            );

            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                logger.info("插入成功");
                return result.buildSuccess(null);
            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                logger.info("更新成功");
                return result.buildSuccess(null);
            }
            // 分片处理信息
            ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                return result.buildFail(ResultConstant.REPLICATION_ERROR_CODE, ResultConstant.REPLICATION_ERROR_MSG);
            }
            // 如果有分片副本失败，可以获得失败原因信息
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                    return result.buildFail(ResultConstant.REPLICATION_ERROR_CODE, failure.reason());
                }
            }
        }
        return new Result().buildFail(ResultConstant.ERROR_CODE, ResultConstant.SUCCEED_MSG);
    }
}
