package com.sscf.games.lib.trace.common.util;

import java.util.Random;
import java.util.UUID;

/**
 * @author jianlong_li
 * @date 2018/11/10.
 */
public class TraceIdUtil {
    private TraceIdUtil() {
    }

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
            'z'};

    public static final String FIRST_SPAN_PARENT_ID = "0";

    /**
     * 生成唯一id
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成16位的随机序列，存在冲突概率（极低）
     */
    public static String generate16Id() {
        Random random = new Random();
        char[] cs = new char[16];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = DIGITS[random.nextInt(DIGITS.length)];
        }
        return new String(cs);
    }

}
