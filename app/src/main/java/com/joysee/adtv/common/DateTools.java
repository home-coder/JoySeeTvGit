package com.joysee.adtv.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class DateTools {
    private static final String TAG = "com.joysee.adtv.common.DateTools";
    public static final int YEAR=0;
    public static final int MONTH=1;
    public static final int DAY=2;
    public static final int HOUR=3;
    public static final int MINUTE=4;
    public static final int SECOND=5;
    
    /**
     * 把"20110302120000"转换为2011年03月02日 12：00：00
     * @return
     */
    public static String formatCurrentTime(String time){
    	if(time!=null && time.length()==14){
    		StringBuffer sb=new StringBuffer();
    		sb.append(time.substring(0,4)+"年");
    		sb.append(time.substring(4,6)+"月");
    		sb.append(time.substring(6,8)+"日 ");
    		sb.append(time.substring(8,10)+":");
    		sb.append(time.substring(10,12)+":");
    		sb.append(time.substring(12,15));
    		return sb.toString();
    	}
    	return null;
    }
    
    public static String formatTimeRange(String begin,String end){
    	if(begin!=null && begin.length()==14 && end!=null && end.length()==14){
    		StringBuffer sb=new StringBuffer();
//    		sb.append(time.substring(0,4)+"年");
    		sb.append(begin.substring(4,6)+"月");
    		sb.append(begin.substring(6,8)+"日 ");
    		sb.append(begin.substring(8,10)+":"); //hour
    		sb.append(begin.substring(10,12)+"-");//minute
    		sb.append(end.substring(8,10));//hour
    		sb.append(":");
    		sb.append(end.substring(10, 12));//minute
    		return sb.toString();
    	}
    	return null;
    }
    
    public static String getStartTime(String begin){
    	if(begin!=null && begin.length()==14){
    		StringBuffer sb=new StringBuffer();
//    		sb.append(time.substring(0,4)+"年");
    		sb.append(begin.substring(4,6)+"月");
    		sb.append(begin.substring(6,8)+"日 ");
    		sb.append(begin.substring(8,10)+":"); //hour
    		sb.append(begin.substring(10,12));//minute
    		return sb.toString();
    	}
    	return null;
    }
    
    public static long transStringToTimeInMillis(String time_str){
    	if(time_str==null || time_str.equals("")){
    		return 0;
    	}
    	System.out.println("time_str-------------->"+time_str);
    	SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMddHHmmss");
//    	time=formatter1.format(formatter2.parse(time));
    	Date date=null;
    	try {
			date=formatter.parse(time_str);
			if(date!=null){
				return date.getTime();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return 0;
    }
    
    /**
     * //将long字符串转换成格式时间输出
     * @param time
     * @return
     */
    public static String transTimeInMillisToString(long time){
    	Date date=new Date(time);
    	SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMddHHmmss");
    	String s=formatter.format(date);
    	System.out.println("s------------>"+s);
    	return s;
    }
    
    public static int getMaxDayOfMonth(int year, int month) {
        int days[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        if (2 == month && 0 == (year % 4)
                && (0 != (year % 100) || 0 == (year % 400))) {
            days[1] = 29;
        }
        return (days[month - 1]);
    }
    
    public static String getHourMinute(long time){
    	Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        String hour;
        if(c.get(Calendar.HOUR_OF_DAY)<10){
        	hour="0"+c.get(Calendar.HOUR_OF_DAY);
        }else{
        	hour=c.get(Calendar.HOUR_OF_DAY)+"";
        }
        String minute;
        if(c.get(Calendar.MINUTE)<10){
        	minute="0"+c.get(Calendar.MINUTE);
        }else{
        	minute=c.get(Calendar.MINUTE)+"";
        }
        return hour+":"+minute;
    }
    
    public static String getHourMinuteSecond(long time){
    	Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        String hour;
        if(c.get(Calendar.HOUR_OF_DAY)<10){
        	hour="0"+c.get(Calendar.HOUR_OF_DAY);
        }else{
        	hour=c.get(Calendar.HOUR_OF_DAY)+"";
        }
        String minute;
        if(c.get(Calendar.MINUTE)<10){
        	minute="0"+c.get(Calendar.MINUTE);
        }else{
        	minute=c.get(Calendar.MINUTE)+"";
        }
        String second;
        if(c.get(Calendar.SECOND)<10){
        	second="0"+c.get(Calendar.SECOND);
        }else{
        	second=c.get(Calendar.SECOND)+"";
        }
        return hour+":"+minute+":"+second;
    }
    

    /**
     * 
     * @param time
     *            //毫秒
     * @return
     */
    public static String getDate(long time,int max) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
//        Date d = c.getTime();
//        Log.d(TAG, "date--->" + d.toString());
        String month;
        if(c.get(Calendar.MONTH)<10){
        	month="0"+c.get(Calendar.MONTH);
        }else{
        	month=c.get(Calendar.MONTH)+"";
        }
        String day;
        if(c.get(Calendar.DAY_OF_MONTH)<10){
        	day="0"+c.get(Calendar.DAY_OF_MONTH);
        }else{
        	day=c.get(Calendar.DAY_OF_MONTH)+"";
        }
        String hour;
        if(c.get(Calendar.HOUR_OF_DAY)<10){
        	hour="0"+c.get(Calendar.HOUR_OF_DAY);
        }else{
        	hour=c.get(Calendar.HOUR_OF_DAY)+"";
        }
        String minute;
        if(c.get(Calendar.MINUTE)<10){
        	minute="0"+c.get(Calendar.MINUTE);
        }else{
        	minute=c.get(Calendar.MINUTE)+"";
        }
        Log.d(TAG, " minute="+minute);
        String second;
        if(c.get(Calendar.SECOND)<10){
        	second="0"+c.get(Calendar.SECOND);
        }else{
        	second=c.get(Calendar.SECOND)+"";
        }
        String result="";
        if(max==MONTH){
        	result=month + "月"
                    + day + "日 " + hour + ":"
                    + minute + ":" + second;
        }else if(max==DAY){
        	result=day + "日 " + hour + ":"
                    + minute + ":" + second;
        }else if(max==HOUR){
        	result=hour + ":"
                    + minute + ":" + second;
        }
        return result;
    }
}
