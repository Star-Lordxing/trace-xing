package com.sscf.games.lib.trace.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.sscf.games.lib.trace.common.exception.JsonConvertException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author jianlong_li
 * @date 2018/11/14 18:39
 */
public class ObjectUtil {
    private ObjectUtil() {
    }

    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
    private static final ObjectMapper PRETTY_DATE_MAPPER = new ObjectMapper();

    static {
        SerializationConfig config = PRETTY_DATE_MAPPER.getSerializationConfig().with(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        PRETTY_DATE_MAPPER.setConfig(config);
    }

    /**
     * 转换为json字符串（Date会转换为时间戳）
     */
    public static String toJsonStr(Object object) {
        if (object == null) {
            return Objects.toString(null);
        }
        try {
            return DEFAULT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(e);
        }
    }

    /**
     * 转换为Tag专用的json字符串（Date会转换为"yyyy-MM-dd HH:mm:ss"，引用用''）
     */
    public static String toTagJsonStr(Object object) {
        if (object == null) {
            return Objects.toString(null);
        }
        try {
            return PRETTY_DATE_MAPPER.writeValueAsString(object).replace("\"", "'");
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(e);
        }
    }

    public static String getMethodName(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }

    /**
     * 将入参数组转换为Map
     */
    public static Map<String, Object> buildArgsMap(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Map<String, Object> argsMap = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            argsMap.put(String.format("args[%d]", i), args[i]);
        }
        return argsMap;
    }

    /**
     * 判断class是否为基础类型
     */
    public static boolean isBaseDataType(Class clazz) {
        return (clazz.isPrimitive() || clazz == String.class || clazz == Integer.class || clazz == Byte.class || clazz == Long.class
                || clazz == Double.class || clazz == Float.class || clazz == Character.class || clazz == Short.class
                || clazz == BigDecimal.class || clazz == BigInteger.class || clazz == Boolean.class
        );
    }

    /**
     * 通过反射获取对象中某个名称的值
     *
     * @param obj       对象
     * @param fieldName 字段
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj.getClass();
        Field field = ReflectionUtils.findField(clazz, fieldName);
        if (field == null) {
            return null;
        }
        field.setAccessible(true);
        return ReflectionUtils.getField(field, obj);
    }

}
