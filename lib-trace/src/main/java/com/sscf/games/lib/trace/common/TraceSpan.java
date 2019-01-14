package com.sscf.games.lib.trace.common;

import com.sscf.games.lib.trace.common.constant.TraceTagConstants;
import com.sscf.games.lib.trace.common.em.TraceResultTypeEnum;
import com.sscf.games.lib.trace.common.em.TraceTypeEnum;
import com.sscf.games.lib.trace.common.util.ObjectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
@Data
@Slf4j
public class TraceSpan {

    private String traceId;
    private String parentId;
    private String id;

    /**
     * span名称
     * http为url(https://www.sui.com/login)，
     * rpc为interface#method（如com.finance.LoginService#login）
     * redis为具体调用方法interface#method
     */
    private String name;
    private TraceTypeEnum traceTypeEnum;
    private TraceResultTypeEnum resultTypeEnum;
    private Long start;
    private Long duration;

    private ServicePoint localPoint;
    private ServicePoint remotePoint;

    private Throwable exception;
    private Map<String, String> tagMap = new LinkedHashMap<>();

    public TraceSpan(TraceTypeEnum traceTypeEnum) {
        this.traceTypeEnum = traceTypeEnum;
    }

    /**
     * 开始计时
     */
    public void startCount() {
        this.start = System.currentTimeMillis();
    }

    /**
     * 停止计时
     */
    public void stopCount() {
        long endMillis = System.currentTimeMillis();
        this.duration = endMillis - this.start;
    }

    /**
     * 增加单个Tag（注：空值不会添加），过长会进行截取
     */
    public void addTag(String tagKey, Object tagValue) {
        if (tagValue == null) {
            return;
        }
        if (tagValue instanceof String &&
                TraceTagConstants.EMPTY_STR.equals(tagValue)) {
            return;
        }
        String value;
        if (ObjectUtil.isBaseDataType(tagValue.getClass())) {
            value = Objects.toString(tagValue);
        } else {
            value = ObjectUtil.toTagJsonStr(tagValue);
        }
        tagMap.put(tagKey, value);
    }

    /**
     * 增加Tag（以map）
     */
    public void addTagMap(Map<String, ?> map) {
        if (map == null || map.size() == 0) {
            return;
        }
        for (Map.Entry<String, ?> tagEntry : map.entrySet()) {
            this.addTag(tagEntry.getKey(), tagEntry.getValue());
        }
    }

    /**
     * 根据key获取tag
     */
    public String getTag(String tagKey) {
        if (tagMap == null) {
            return null;
        }
        return tagMap.get(tagKey);
    }

    public String getExceptionMsg() {
        if (exception == null || StringUtils.isEmpty(exception.getMessage())) {
            return null;
        }
        return exception.getMessage();
    }
}
