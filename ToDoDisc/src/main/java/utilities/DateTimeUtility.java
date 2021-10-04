package utilities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtility {
	final static long DAYS_TO_SEC = 86400L;
	final static long HOURS_TO_SEC = 3600L;
	final static long MINUTES_TO_SEC = 60L;
	final static long SECONDS_TO_SEC = 1L;
	
	public static String getTimeSecond() {
		int[] hms = new int[3];
		String[] hmsS = new String[3];
		
		hms[0] = LocalTime.now().getHour();
		hms[1] = LocalTime.now().getMinute();
		hms[2] = LocalTime.now().getSecond();
		
		for(int i=0; i<3; i++) {
			if(hms[i]<10) { 
				hmsS[i] = "0"+hms[i];
			} else {
				hmsS[i] = Integer.toString(hms[i]);
			}
		}
		
		return hmsS[0]+":"+hmsS[1]+":"+hmsS[2];
	}
	
	public static String[] getDateTimeOffset(Duration dur) {
		LocalDateTime dt = LocalDateTime.now().plus(dur);
		String[] res = new String[2];
		
		res[0] = dt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
		res[1] = dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		
		return res;
	}
	
	public static String[] getDateTimeNow(String timeFormat, String yearFormat) {
		LocalDateTime dt = LocalDateTime.now();
		String[] res = new String[2];
		
		res[0] = dt.toLocalDate().format(DateTimeFormatter.ofPattern(yearFormat));
		res[1] = dt.toLocalTime().format(DateTimeFormatter.ofPattern(timeFormat));
		
		return res;
	}
	
	public static String[] getDateTimeNow() {
		LocalDateTime dt = LocalDateTime.now();
		String[] res = new String[2];
		
		res[0] = dt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
		res[1] = dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		
		return res;
	}
	
	public static long getOffsetSeconds(int targetHr, int targetMin) {
		int currentHr = LocalTime.now().getHour();
		int currentMin = LocalTime.now().getMinute();
		long res = 0;
		
		//next day
		if((targetHr < currentHr) || ((targetHr == currentHr) && (targetMin < currentMin))) { 
			LocalDateTime target = LocalDateTime.now().plusDays(1).withMinute(targetMin).withHour(targetHr).withSecond(0);
			LocalDateTime now = LocalDateTime.now();
			res = ChronoUnit.SECONDS.between(now, target);
		} else if (targetHr > currentHr || (targetHr == currentHr) && (targetMin > currentMin)){ 
			LocalDateTime target = LocalDateTime.now().withMinute(targetMin).withHour(targetHr).withSecond(0);
			LocalDateTime now = LocalDateTime.now();
			res = ChronoUnit.SECONDS.between(now, target);
		} else {
			res = 0;
		}
		
		return res;
	}
	
	public static String formatDuration(Duration d) { 
		long[] dhm = new long[3];
		String msg = "";
		
		long seconds = d.toSeconds();
		dhm[0] = seconds / DAYS_TO_SEC;
		seconds = seconds % DAYS_TO_SEC;
		dhm[1] = seconds / HOURS_TO_SEC;
		seconds = seconds % HOURS_TO_SEC;
		dhm[2] = seconds / MINUTES_TO_SEC;
		
		if(dhm[0] > 0) { 
			msg += dhm[0] + " Days, ";
		} 
		if(dhm[1] > 0) {
			msg += dhm[1] + " Hours, ";
		}
		if(dhm[2] > 0) {
			msg += dhm[2] + " Minutes";
		}
		
		return msg;
	}
}
