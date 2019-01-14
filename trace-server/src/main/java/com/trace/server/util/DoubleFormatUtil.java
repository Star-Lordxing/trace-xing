package com.trace.server.util;

import java.math.BigDecimal;

public class DoubleFormatUtil {
    public static double format(Double value) {
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double formatToSeconds(Double value) {
        if (value == null || Double.isInfinite(value) ||Double.isNaN(value)) {
            return 0D;
        }
        BigDecimal bg = new BigDecimal(value);
        return bg.divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String formatPercent(int value1, int value2) {
        if (value1 == 0 || value2 == 0) {
            return "0";
        }
        BigDecimal bg1 = new BigDecimal(value1);
        BigDecimal bg2 = new BigDecimal(value2);
        return bg1.divide(bg2, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%";
    }
}
