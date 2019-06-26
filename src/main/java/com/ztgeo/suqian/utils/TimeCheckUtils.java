package com.ztgeo.suqian.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeCheckUtils {
    /**
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return true在时间段内，false不在时间段内
     * @throws Exception
     */
    public static boolean hourMinuteBetween(String startDate, String endDate){

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            Date now = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            Date start = simpleDateFormat.parse(startDate);
            Date end = simpleDateFormat.parse(endDate);

            long nowTime = now.getTime();
            long startTime = start.getTime();
            long endTime = end.getTime();

            System.out.println(nowTime + "\t" + startTime + "\t" + endTime);

            return nowTime >= startTime && nowTime <= endTime;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("调用<判断一个时间是否在某一个时间段的范围内>方法异常！");
        }
    }
}
