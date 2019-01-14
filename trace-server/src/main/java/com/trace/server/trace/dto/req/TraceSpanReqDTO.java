package com.trace.server.trace.dto.req;

import com.trace.server.es.bean.Page;
import lombok.Data;

import java.util.Calendar;
import java.util.Date;

/**
 * @author jianlong_li
 * @date 2018/11/12 19:23
 */
@Data
public class TraceSpanReqDTO extends Page {
    private String traceId;
    private String name;
    private String traceType;
    private String resultType;
    private String localServiceName;
    private Long start;


    public static Date getStartDate(TraceSpanReqDTO traceSpanReqDTO) {

        if (traceSpanReqDTO != null && traceSpanReqDTO.getStart() != null) {
            Date date = new Date();//获取当前时间    
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            if (traceSpanReqDTO.getStart() == 1L) {
                calendar.add(Calendar.HOUR, -1);
            } else if (traceSpanReqDTO.getStart() == 2L) {
                calendar.add(Calendar.DATE, -1);
            } else if (traceSpanReqDTO.getStart() == 3L) {
                calendar.add(Calendar.DAY_OF_WEEK, -1);
            } else if (traceSpanReqDTO.getStart() == 4L) {
                calendar.add(Calendar.MONTH, -3);
            }
            date = calendar.getTime();
            return date;
        }
        return null;
    }

}
