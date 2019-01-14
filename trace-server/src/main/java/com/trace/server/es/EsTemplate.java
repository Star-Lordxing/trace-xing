package com.trace.server.es;

import com.trace.server.es.bean.AggEntity;
import com.trace.server.es.bean.Page;
import com.trace.server.es.bean.Result;
import com.trace.server.es.dao.HAggDao;
import com.trace.server.es.dao.HQueryDao;
import com.trace.server.es.dao.InsertDao;
import com.trace.server.es.em.TableEnum;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月13日
 * @since 1.0
 */

@Component
public class EsTemplate {
    /**
     * 插入和修改
     */
    public <T> Result insert(TableEnum table, T param) {
        return InsertDao.insert(table, param);
    }

    /**
     * 精确查询
     */
    public <T> Result<Page<T>> queryListByTerm(TableEnum table, Page param, Class<?> respClass) {
        return HQueryDao.queryListByTerm(table, param, respClass);
    }

    /**
     * 聚合查询
     */
    public <T> Map agg(TableEnum table, T param, Map<String, AggEntity> map) {
        return HAggDao.agg(table, param, map);
    }

}
