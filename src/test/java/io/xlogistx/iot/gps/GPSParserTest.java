package io.xlogistx.iot.gps;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.server.util.ServerUtil;


public class GPSParserTest 
{
	

	public static void main(String[] args) throws ParseException
	{
		//GPSLogs is located at "D:\\Temp\\GPS.logs"
		
		String delimiter = "$";
		List<String> list = new ArrayList<String>();
		
		try 
		{
			list = ServerUtil.toStringList("D:\\Temp\\GPS.logs");
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Original List: ");
	    int i = 0;
	    while(i != list.size())
	    {
	    	System.out.println(list.get(i));
	    	i++;
	    }
		
		List<String> parsedList = new ArrayList<String>();
		parsedList = GPSLogParser.logParser(list, delimiter);
		
	    System.out.println("\nLog Filtered List: ");
		for(int j = 0; j < parsedList.size(); j++)
		{
			String msg = parsedList.get(j);
			System.out.println(msg);
		} 
		
		
//		for(String msg:parsedList)
//		{
//			System.out.println(msg);
//			GPSParser.parse(msg, "," );
//		}
		
		
		
		try
		{
			
//		String temp[] = "$GPGGA,234612.000,3402.5623,N,11826.8690,W,2,8,1.35,70.2,M,-33.8,M,0000,0000*5E".split(",");
//		for (  i=0; i<temp.length; i ++)
//		{
//			System.out.println("[" +i+"]" + temp[i]);
//		}
		
		

		for(int k = 0; k < parsedList.size(); k++)
		{
			String msg = parsedList.get(k);
			System.out.println(msg);
			GPSMessage message = GPSParser.parse(msg, ",");
			System.out.println(message);
			System.out.println(GSONUtil.toJSON(message, true));
		}

		
		GPSFixedData fixed = (GPSFixedData) GPSParser.parse("$GPGGA,234612.000,3402.5623,N,11826.8690,W,2,8,1.35,70.2,M,-33.8,M,0000,0000*5E",",");													  	
		System.out.println("Fixed Data:");	
		System.out.println("$GPGGA,234612.000,3402.5623,N,11826.8690,W,2,8,1.35,70.2,M,-33.8,M,0000,0000*5E");
		System.out.println(fixed);
		System.out.println(GSONUtil.toJSON(fixed, true));
		
		GPSActiveSatellites active = (GPSActiveSatellites) GPSParser.parse("$GPGSA,A,3,07,17,09,28,15,26,01,08,,,,,2.21,1.35,1.75*0C", ",");
		System.out.println("Active Satellites:");
		System.out.println("$GPGSA,A,3,07,17,09,28,15,26,01,08,,,,,2.21,1.35,1.75*0C");
		System.out.println(active);
		System.out.println(GSONUtil.toJSON(active, true));
		
		GPSNavigation navig = (GPSNavigation) GPSParser.parse("$GPRMC,234612.000,A,3402.5623,N,11826.8690,W,0.02,302.15,070514,,,D*75", ",");
		System.out.println("Navigation:");
		System.out.println("$GPRMC,234612.000,A,3402.5623,N,11826.8690,W,0.02,302.15,070514,,,D*75");
		System.out.println(navig);
		System.out.println(GSONUtil.toJSON(navig, true));
		
		GPSCourseInfo course = (GPSCourseInfo) GPSParser.parse("$GPVTG,309.62,T,,M,0.13,N,0.2,K,D*06", ",");
		System.out.println("Course Info:");
		System.out.println("$GPVTG,309.62,T,,M,0.13,N,0.2,K*6E");
		System.out.println(course);
		System.out.println(GSONUtil.toJSON(course, true));
		
		GPSGeographicPosition geopos = (GPSGeographicPosition) GPSParser.parse("$GPGLL,3723.2475,N,12158.3416,W,161229.487,A*2C", ",");
		System.out.println("Geographic Position:");
		System.out.println("$GPGLL,3723.2475,N,12158.3416,W,161229.487,A*2C");
		System.out.println(geopos);
		System.out.println(GSONUtil.toJSON(geopos, true));
		
		GPSDateTime date = (GPSDateTime) GPSParser.parse("$GPZDA,064951.000,16,10,2013,08,00*57", ",");
		System.out.println("Date & Time:");
		System.out.println("$GPZDA,064951.000,16,10,2013,08,00*60");
		System.out.println(date);
		System.out.println( new Date( date.getGPSTime()));
		System.out.println(GSONUtil.toJSON(date, true));
		
		GPSSatellitesInView satellite1 = (GPSSatellitesInView) GPSParser.parse("$GPGSV,3,1,09,29,36,029,42,21,46,314,43,26,44,020,43,15,21,321,39*7D", ",");
		System.out.println("Satellites in View 1");
		System.out.println("$GPGSV,3,1,09,29,36,029,42,21,46,314,43,26,44,020,43,15,21,321,39*7D");
		System.out.println(satellite1);
		System.out.println(GSONUtil.toJSON(satellite1, true));
		
		GPSSatellitesInView satellite2 = (GPSSatellitesInView) GPSParser.parse("$GPGSV,3,2,09,18,26,314,40,09,57,170,44,06,20,229,37,10,26,084,37*77", ",");
		System.out.println("Satellites in View 2");
		System.out.println("$GPGSV,3,2,09,18,26,314,40,09,57,170,44,06,20,229,37,10,26,084,37*77");
		System.out.println(satellite2);
		System.out.println(GSONUtil.toJSON(satellite2, true));
		
		GPSSatellitesInView satellite3 = (GPSSatellitesInView) GPSParser.parse("$GPGSV,3,3,09,07,,,26*73", ",");
		System.out.println("Satellites in View 3");
		System.out.println("$GPGSV,3,3,09,07,,,26*73");
		System.out.println(satellite3);
		System.out.println(GSONUtil.toJSON(satellite3, true));
		
		GPSReceiverSignal signal = (GPSReceiverSignal) GPSParser.parse("$GPMSS,55,27,318.0,100,*66", ",");
		System.out.println("Receiver Signal: ");
		System.out.println("$GPMSS,55,27,318.0,100,*66");
		System.out.println(signal);
		System.out.println(GSONUtil.toJSON(signal, true));
		
		
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
        
		
						
	}
	

}
