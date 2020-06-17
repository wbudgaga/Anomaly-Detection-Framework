
package anomalydetection.util;

import java.util.Calendar;

public class UtilDate{
 
	private static int[] monthsLen={31,29,31,30,31,30,31,31,30,31,30,31};
	
	public static  int getDayNumber(int m, int day){
		int sum = 0;
		for (int i=0;i<m-1;++i)
			sum += monthsLen[i];
		return (sum + day);
	}
	
	public static  int getDayMinutes(int h, int m){		
		return  (h * 60 + m);
	}
	
	public static long getTime(int y, int m, int d, int h){
       	Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, y);
        calendar.set(Calendar.MONTH,m);
        calendar.set(Calendar.DAY_OF_MONTH, d);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        return calendar.getTimeInMillis();
	}
	
	public static String getDateString(){
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR)+"_"+(calendar.get(Calendar.MONTH)+1)+"_"+calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	public static String getTimeString(){
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
	}
	public static void main(String[] args){
		//System.out.println(getDayNumber(6,2));
		UtilClass.fileAsOutputDst("/s/chopin/b/grad/budgaga/noaa/walid.txt");
		System.out.println("test test");
		System.out.println("test test1");
		System.out.println("test test2");
		System.out.println("test test4");
		System.out.println("test test5");
		System.out.println("test test1");
		System.out.println("test test11");
		System.out.println("test test21");
		System.out.println("test test41");
		System.out.println("test test51");

	}
}
