package io.xlogistx.iot.net.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.util.LinkLayerAddress;

public class PcapNetUtil {

	public final static PcapNetUtil SINGLETON = new PcapNetUtil();
	private List<PcapNetworkInterface> allDevs = null;
	
	private PcapNetUtil() {}
	
	
	public PcapNetworkInterface getPcapNetworkInterface(String niName) throws IOException
	{
		if (allDevs == null)
		{
			synchronized(this)
			{
				if (allDevs == null)
				{
					try 
					{
						allDevs = Pcaps.findAllDevs();
					} catch (PcapNativeException e) {
						throw new IOException(e.getMessage());
					}
				}
			}
		}
	
	    NetworkInterface ni = NetworkInterface.getByName(niName);
	    if (ni != null)
	    {
	    	for(PcapNetworkInterface pNI: allDevs)
	    	{
	    		ArrayList<LinkLayerAddress> llla = pNI.getLinkLayerAddresses();
	    		if (llla != null && llla.size() > 0)
	    		{
		    		for (LinkLayerAddress lla : llla)
		    		{
		    			if (Arrays.equals(ni.getHardwareAddress(), lla.getAddress()))
		    			{
		    				return pNI;
		    			}
		    		}
	    		}
	    		
	    		System.out.println(pNI.getName());
	    	}
	    }
	    
	    
	    return null;
	}
	
	
	public  NIPMap[] getNIPMaps() throws IOException
	{
		Enumeration<NetworkInterface> eNI = NetworkInterface.getNetworkInterfaces();
	
		List<NIPMap> ret = new ArrayList<NIPMap>();
		while(eNI.hasMoreElements())
		{
			NetworkInterface ni = eNI.nextElement();
			PcapNetworkInterface pni = getPcapNetworkInterface(ni.getName());
			if (pni != null)
			{
				ret.add(new NIPMap(ni, pni));
			}
		}

	    
	    return ret.toArray(new NIPMap[ret.size()]);
	}
	
	 public static String readInput() throws IOException 
	 {
		 BufferedReader reader  = new BufferedReader(new InputStreamReader(System.in));
		 return reader.readLine();
	 }
}
