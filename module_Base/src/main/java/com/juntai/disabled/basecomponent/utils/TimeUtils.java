package com.juntai.disabled.basecomponent.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: tobato
 * @Description: 作用描述  和时间日期相关的类
 * @CreateDate: 2020/7/15 10:55
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/15 10:55
 */
public class TimeUtils {

    /**
     * 根据时间字符串获取毫秒数
     *
     * @param strTime
     * @return
     */
    private static long getTimeMillis(SimpleDateFormat sdf,String strTime) {
        long returnMillis = 0;
        Date d = null;
        try {
            d = sdf.parse(strTime);
            returnMillis = d.getTime();
        } catch (ParseException e) {
        }
        return returnMillis;
    }

    /**
     * 传入开始时间和结束时间字符串来计算消耗时长
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getTimeExpend(SimpleDateFormat sdf ,String startTime, String endTime) {
        //传入字串类型 2016/06/28 08:30
        long longStart = getTimeMillis(sdf,startTime); //获取开始时间毫秒数
        long longEnd = getTimeMillis(sdf,endTime);  //获取结束时间毫秒数
        long longExpend = longEnd - longStart;  //获取时间差

        long longHours = longExpend / (60 * 60 * 1000); //根据时间差来计算小时数
        long longMinutes = (longExpend - longHours * (60 * 60 * 1000)) / (60 * 1000);   //根据时间差来计算分钟数
        return longHours+"小时"+longMinutes+"分";
    }
    /**
     * 传入开始时间和结束时间字符串来计算消耗时长
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static long getTimeHourExpend(SimpleDateFormat sdf ,String startTime, String endTime) {
        //传入字串类型 2016/06/28 08:30
        long longStart = getTimeMillis(sdf,startTime); //获取开始时间毫秒数
        long longEnd = getTimeMillis(sdf,endTime);  //获取结束时间毫秒数
        long longExpend = longEnd - longStart;  //获取时间差

        long longHours = longExpend / (60 * 60 * 1000); //根据时间差来计算小时数
        return longHours;
    }
    /**
     * 比较时间大小
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static boolean compareTimes(SimpleDateFormat sdf ,String startTime, String endTime) {
        //传入字串类型 2016/06/28 08:30
        long longStart = getTimeMillis(sdf,startTime); //获取开始时间毫秒数
        long longEnd = getTimeMillis(sdf,endTime);  //获取结束时间毫秒数
       return longStart>longEnd;
    }

    /**
     * 传入结束时间和消耗时长来计算开始时间
     *
     * @param endTime
     * @param expendTime
     * @return
     */
    private String getTimeString(SimpleDateFormat sdf ,String endTime, String expendTime) {
        //传入字串类型 end:2016/06/28 08:30 expend: 03:25
        long longEnd = getTimeMillis(sdf,endTime);
        String[] expendTimes = expendTime.split(":");   //截取出小时数和分钟数
        long longExpend = Long.parseLong(expendTimes[0]) * 60 * 60 * 1000 + Long.parseLong(expendTimes[1]) * 60 * 1000;
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return sdfTime.format(new Date(longEnd - longExpend));
    }

    /**
     * 通过传入的开始时间 判断出当天时间的最小时间和最大时间
     * @param sdf
     * @param startTime
     * @param timeCondition  08:00
     */
    private static  String getTimeOfDayByCondition(SimpleDateFormat sdf ,String  startTime,String timeCondition){
        String time = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
           Date  date =  sdf.parse(startTime);
         time  =   simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time+" "+timeCondition;
    }

    /**
     * 通过传入的开始时间 判断出当天时间的最小时间和最大时间
     * 是否能够开始服务
     * @param sdf
     * @param startTime
     * @param minTime  08:00
     * @param maxTime  16:30
     */
    public static  boolean isOKTimeToStartService(SimpleDateFormat sdf ,String  startTime,String minTime,
                                                  String maxTime){
        //开始时间大于最早时间
       boolean moreThanTheMinStartTime =  compareTimes(sdf,startTime,getTimeOfDayByCondition(sdf,startTime,minTime));
        //开始时间小于最晚时间
       boolean lessThanTheMaxStartTime =  compareTimes(sdf,getTimeOfDayByCondition(sdf,startTime,maxTime),startTime);
        return moreThanTheMinStartTime&&lessThanTheMaxStartTime;
    }




}
