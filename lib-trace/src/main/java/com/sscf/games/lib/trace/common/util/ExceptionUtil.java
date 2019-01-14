package com.sscf.games.lib.trace.common.util;

import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

/**
 * @author jianlong_li
 * @date 2018/11/16 15:15
 */
public class ExceptionUtil {
    private ExceptionUtil() {
    }

    public static Throwable unWarp(Throwable throwable) {
        Throwable unWarpException = throwable;
        boolean isUnWarp = false;
        while (!isUnWarp) {
            isUnWarp = true;
            if (unWarpException instanceof ServletException && unWarpException.getCause() != null &&
                    !isEqualException(unWarpException, unWarpException.getCause())) {
                isUnWarp = false;
                unWarpException = unWarpException.getCause();
            }
            if (unWarpException instanceof UndeclaredThrowableException) {
                isUnWarp = false;
                unWarpException = ((UndeclaredThrowableException) unWarpException).getUndeclaredThrowable();
            }
            if (unWarpException instanceof InvocationTargetException) {
                isUnWarp = false;
                unWarpException = ((InvocationTargetException) unWarpException).getTargetException();
            }
        }
        return unWarpException;
    }

    public static boolean containsExceptionType(Throwable throwable, Class<? extends Throwable> exceptionType) {
        if (throwable == null) {
            return false;
        }
        if (isExceptionType(throwable.getClass(), exceptionType)) {
            return true;
        }
        Throwable before = throwable;
        Throwable cause = throwable.getCause();
        while (cause != null && !isEqualException(before, cause)) {
            if (isExceptionType(cause.getClass(), exceptionType)) {
                return true;
            }
            before = cause;
            cause = before.getCause();
        }
        return cause != null && isExceptionType(cause.getClass(), exceptionType);
    }

    private static boolean isEqualException(Throwable before, Throwable cause) {
        return before.getClass().equals(cause.getClass()) && Objects.equals(before.getMessage(), cause.getMessage());
    }


    private static boolean isExceptionType(Class<? extends Throwable> causeClass, Class<? extends Throwable> exceptionType) {
        return causeClass.equals(exceptionType) || causeClass.isAssignableFrom(exceptionType);
    }
}
