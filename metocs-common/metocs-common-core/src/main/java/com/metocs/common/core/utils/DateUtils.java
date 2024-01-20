package com.metocs.common.core.utils;

import com.metocs.common.core.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public enum Pattern{

        YEAR_MONTH_DAY_HOUR_MIN_SEC,

        YEAR_MONTH_DAY,

        YEAR_MONTH,
    }

    private static final SimpleDateFormat YEAR_MONTH_DAY_HOUR_MIN_SEC_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat YEAR_MONTH_DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");

    public static String format(Date date,Pattern pattern){
        logger.debug("时间转换 date -> string: {}  时间格式为 {}",date,pattern);
        switch (pattern){
            case YEAR_MONTH_DAY_HOUR_MIN_SEC:
                return YEAR_MONTH_DAY_HOUR_MIN_SEC_FORMAT.format(date);
            case YEAR_MONTH_DAY:
                YEAR_MONTH_DAY_FORMAT.format(date);
            case YEAR_MONTH:
                YEAR_MONTH_FORMAT.format(date);
            default:
                throw new CommonException("暂不支持该时间格式！");
        }
    }


    public static Date parse(String date,Pattern pattern){
        logger.debug("时间转换 string -> data: {}  时间格式为 {}",date,pattern);
        try {
            switch (pattern){
                case YEAR_MONTH_DAY_HOUR_MIN_SEC:
                    return YEAR_MONTH_DAY_HOUR_MIN_SEC_FORMAT.parse(date);
                case YEAR_MONTH_DAY:
                        YEAR_MONTH_DAY_FORMAT.parse(date);
                case YEAR_MONTH:
                    YEAR_MONTH_FORMAT.parse(date);
                default:
                    throw new CommonException("暂不支持该时间格式！");
            }
        } catch (ParseException e) {
            logger.error("时间转换错误 {}",e.getMessage(),e);
            throw new CommonException("时间转换错误！");
        }
    }
}
