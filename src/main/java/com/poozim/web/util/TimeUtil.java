package com.poozim.web.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
	public static String getDate() {
		Date d = new Date();
		
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd HHmmss", Locale.KOREA);
		dtFormat.applyPattern("yyyy-MM-dd");
	    return dtFormat.format(d);
	}

	public static String getDateTime() {
	    Date d = new Date();
	
	    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd HHmmss", Locale.KOREA);
	    dtFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
	    return dtFormat.format(d);
	}
	
	public static String getDateTimeString() {
	    Date d = new Date();
	
	    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd HHmmss", Locale.KOREA);
	    dtFormat.applyPattern("yyyyMMddHHmmss");
	    return dtFormat.format(d);
	}
	
}
