package com.webmagic.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class DateUtil {
	public final static String YYYYMMDDHH24MISS = "yyyyMMddHHmmss";
	public final static String YYYY_MM_DD_HH24MISS = "yyyy-MM-dd HH:mm:ss";
	public final static String YYYYMMDD = "yyyyMMdd";
	public final static String YYYYMM = "yyyyMM";
	public final static String YYYYMMHH = "yyyyMMddHH";
	public final static String YYYY = "yyyy";
	public final static String HH24MISS = "HHmmss";
	public final static String YYYYMMDDHH24MISSSSS = "yyyyMMddHHmmssSSS";

	/**
	 * 获取当前时间.
	 * 
	 * @return 返回时间格式为YYYYMMDDHH24MISS的字符串
	 */
	public static String getCurrDate() {
		return formatDate(new Date(), YYYYMMDDHH24MISS);
	}
	
	public static String getCurrYear() {
		return formatDate(new Date(), YYYY);
	}

	/**
	 * 获取当前时间.
	 * 
	 * @param dateFormat
	 *            时间格式
	 * @return 返回指定时间格式的字符串
	 */
	public static String getCurrDate(String dateFormat) {
		return formatDate(new Date(), dateFormat);
	}

	/**
	 * 将时间格式为YYYYMMDDHH24MISS的字符串转化为Date.
	 * 
	 * @param dateStr
	 *            时间格式为YYYYMMDDHH24MISS的字符串
	 * @return Date
	 */
	public static Date parseDate(String dateStr) {
		if (dateStr.length() > 14) {
			dateStr = dateStr.substring(0, 14);
		}
		switch (dateStr.length()) {
		case 6:
			return parseDate(dateStr, YYYYMM);
		case 8:
			return parseDate(dateStr, YYYYMMDD);
		case 14:
			return parseDate(dateStr, YYYYMMDDHH24MISS);
		default:
			return null;
		}
	}

	/**
	 * 根据给定的格式把时间字符串转化为Date.
	 * 
	 * @param dateStr
	 *            dateFormat时间格式的字符串
	 * @param dateFormat
	 *            时间格式
	 * @return Date
	 * @throws Exception 
	 */
	public static Date parseDate(String dateStr, String dateFormat) {
		/*
		 * if (StringHelper.isEmpty(dateStr)) { return null; }
		 */

		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		try {

			int maxLen = 0;
			if (dateFormat.equals(YYYYMMDDHH24MISS)) {
				maxLen = 14;
			} else if (dateFormat.equals(YYYYMMDD)) {
				maxLen = 8;
			} else if (dateFormat.equals(YYYYMM)) {
				maxLen = 6;
			}

			if (maxLen != 0) {
				dateStr = StringUtils.substring(dateStr, 0, maxLen);
			}

			return df.parse(dateStr);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Date转化为YYYYMMDDHH24MISS格式的字符串.
	 * 
	 * @param date
	 *            Date
	 * @return YYYYMMDDHH24MISS格式的字符串
	 */
	public static String formatDate(Date date) {
		return formatDate(date, YYYYMMDDHH24MISS);
	}

	/**
	 * 日期格式化处理,去掉时或分或秒,即：将时分秒设置为0 formatDateToDate
	 * 
	 * @author liangzhm
	 * @param date
	 * @param format
	 * @return
	 * @创建日期：2015年6月8日下午3:07:12
	 */
	public static Date formatDateToDate(Date date, String format) {
		String _date = formatDate(date, format);
		return parseDate(_date, format);
	}

	/**
	 * 月份处理，在当前日期上增加或减少月份 operationMonth
	 * 
	 * @author liangzhm
	 * @param currentDate
	 * @param num
	 * @return
	 * @创建日期：2015年6月8日下午3:13:55
	 */
	public static Date operationMonth(Date currentDate, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.MONTH, num);
		return calendar.getTime();
	}

	/**
	 * Date转化为dateFormat时间格式的字符串
	 * 
	 * @param date
	 *            Date
	 * @param dateFormat
	 *            时间格式
	 * @return dateFormat时间格式的字符串
	 */
	public static String formatDate(Date date, String dateFormat) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		return df.format(date);
	}

	/**
	 * 验证时间有效性 .
	 * 
	 * @param strDateTime
	 * @return
	 * @author caiwzh 2015年6月16日 caiwzh
	 */
	public static boolean checkDateFormatAndValite(String strDateTime,
			String dateFormat) {
		// update it according to your requirement.
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		try {
			Date ndate = format.parse(strDateTime);
			String str = format.format(ndate);
			// System.out.println(ndate);
			// System.out.println(str);
			// System.out.println("strDateTime=" + strDateTime);
			// success
			if (str.equals(strDateTime))
				return true;
			// datetime is not validate
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			// format error
			return false;
		}
	}
	
	public static void main(String[] argc) {
		System.out.println("" + "12:00".split(":")[0]);
	}

}
