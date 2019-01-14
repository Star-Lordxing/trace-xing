package com.trace.server.util;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.lang.reflect.Field;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月22日
 * @since 1.0
 */
public class ParamUtil {
    public static BoolQueryBuilder buildParam(Object obj) {
        BoolQueryBuilder mustQuery = QueryBuilders.boolQuery();

        if (obj == null) {
            return mustQuery;
        }

        // 得到类对象
        Class userCla = (Class) obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = userCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            f.setAccessible(true); // 设置些属性是可以访问的
            Object val = new Object();
            try {
                val = f.get(obj);
                if (val != null && !"start".equals(f.getName()) && !"".equals(val)) {
                    // 设置查询条件、field、 value
                    mustQuery.must(QueryBuilders.termQuery(f.getName() + ".keyword", val));
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mustQuery;
    }
}
