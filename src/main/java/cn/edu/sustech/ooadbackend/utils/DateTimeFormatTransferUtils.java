package cn.edu.sustech.ooadbackend.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期时间格式转换工具类
 * @author DYH
 * @version 1.0
 * @className DateTimeFormatTransferUtils
 * @since 2023/9/20 15:13
 */
public class DateTimeFormatTransferUtils {
    
    /**
     * 将前端dateTime类型的日期时间转换为后端Date类型的日期时间
     * @param dateTime 前端dateTime类型的日期时间
     * @return 后端Date类型的日期时间
     */
    public static Date frontDateTime2BackDate(String dateTime) {
        if (StringUtils.isBlank(dateTime)) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
        } catch (ParseException e) {
            return null;
        }
    }
}
