package com.fhgl.shop.utils;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * 订单表分表工具类
 *
 * @author admin
 */

public class OrderTableShardingUtils {

    /**
     * 通过订单号获取表名
     *
     * @param orderId
     * @return
     */
    public static String getTableNameByOrderId(String orderId) {
        if (orderId.length() == 14) {
            return "tb_shop_order_2018";
        } else if (orderId.length() == 19) {
            String timeStr = orderId.substring(2, 15);
            long time = System.currentTimeMillis();  //当前时间的时间戳
            long zero = time / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset() + 1000 * 3600 * 24;//明天的0点
            if (Long.parseLong(timeStr) >= zero - 1000 * 3600 * 24 * 90L) {
                return "tb_shop_order";
            }
            Date date = new Date(Long.parseLong(timeStr));
            int year = ShardingUtil.getYear(date);
            int season = ShardingUtil.getSeason(date);
            String tableName = "tb_shop_order";
            if (year == 2018) {
                tableName = "tb_shop_order_" + year;
            } else {
                tableName = "tb_shop_order_" + year + "_" + season;
            }
            return tableName;
        } else {
            return null;
        }
    }

    /**
     * 通过时间段获取表名列表
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    public static List<String> getTableNameListByTimes(String beginTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> tableNameList = new ArrayList<>();
        try {
            Date beginDate = sdf.parse(beginTime);
            Date endDate = sdf.parse(endTime);

            long time = System.currentTimeMillis();  //当前时间的时间戳
            long zero = time / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset() + 1000 * 3600 * 24;//明天的0点
            if (beginDate.getTime() > endDate.getTime() || endDate.getTime() > time) {
                return null;
            }
            if (beginDate.getTime() >= zero - 1000 * 3600 * 24 * 90L) {
                tableNameList.add("tb_shop_order");
                return tableNameList;
            }
            if (endDate.getTime() <= sdf.parse("2019-01-01 00:00:00").getTime()) {
                tableNameList.add("tb_shop_order_2018");
                return tableNameList;
            }
            if (beginDate.getTime() < sdf.parse("2019-01-01 00:00:00").getTime()) {
                tableNameList.add("tb_shop_order_2018");
                beginDate = sdf.parse("2019-01-01 00:00:00");
            }
            int beginYear = ShardingUtil.getYear(beginDate);
            int beginSeason = ShardingUtil.getSeason(beginDate);

            int endYear = ShardingUtil.getYear(endDate);
            int endSeason = ShardingUtil.getSeason(endDate);
            if (beginYear == endYear) {
                for (int season = beginSeason; season <= endSeason; season++) {
                    String tableName = "";
                    tableName = "tb_shop_order_" + beginYear + "_" + season;
                    tableNameList.add(tableName);
                }
            } else {
                for (int year = beginYear; year <= endYear; year++) {
                    String tableName = "";
                    if (year == beginYear) {
                        for (int season = beginSeason; season <= 4; season++) {
                            tableName = "tb_shop_order_" + year + "_" + season;
                            tableNameList.add(tableName);
                        }
                    } else if (year == endYear) {
                        for (int season = 1; season <= endSeason; season++) {
                            tableName = "tb_shop_order_" + year + "_" + season;
                            tableNameList.add(tableName);
                        }
                    } else {
                        for (int season = 1; season <= 4; season++) {
                            tableName = "tb_shop_order_" + year + "_" + season;
                            tableNameList.add(tableName);
                        }
                    }
                }
            }

            if (endDate.getTime() >= zero - 1000 * 3600 * 24 * 10) {
                tableNameList.add("tb_shop_order");
            }

        } catch (Exception e) {
            return null;
        }
        return tableNameList;
    }

    /**
     * 格式化开始结束时间，防止数据重复
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    public static Map<String, Object> parseBeginTimeAndEndTime(String beginTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date beginDate = sdf.parse(beginTime);
            Date endDate = sdf.parse(endTime);
            long time = System.currentTimeMillis();  //当前时间的时间戳
            long zero = time / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset() + 1000 * 3600 * 24;//明天的0点
            long pre100Zero = zero - 1000 * 3600 * 24 * 90L;
            if (beginDate.getTime() >= pre100Zero) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("beginTime", beginTime);
                map.put("endTime", endTime);
                return map;
            }
            if (endDate.getTime() > pre100Zero) {
                endDate = new Date(pre100Zero);
                endTime = sdf.format(endDate);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            Integer month = calendar.get(Calendar.MONTH) / 3 * 3 + 1;
            calendar.set(calendar.get(Calendar.YEAR), month, 1, 0, 0, 0);
            if (calendar.getTimeInMillis() > beginDate.getTime()) {
                beginDate = calendar.getTime();
                beginTime = sdf.format(beginDate);
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            return map;
        } catch (Exception e) {
            return null;
        }

    }

    public static Map<String, Object> Obj2Map(Object obj) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

    public static boolean isTodayOrder(String orderNo) {

        if (StringUtils.isBlank(orderNo)) {
            return false;
        }
        long time = Long.parseLong(orderNo.substring(2, 15));
        ZoneId z = ZoneId.systemDefault();//获取时区
        ZonedDateTime dt = ZonedDateTime.now(z);//获取当前时间
        long start = dt.toLocalDate().atStartOfDay(z).toEpochSecond() * 1000;//获取当天的起始时间戳
        long end = start + 24L * 3600 * 1000;//获取当天的结束时间戳

        return start <= time && time <= end;
    }


}







