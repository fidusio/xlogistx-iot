package io.xlogistx.iot.net.util;

import java.net.NetworkInterface;

import org.pcap4j.core.PcapNetworkInterface;

public class NIPMap 
{
	public final NetworkInterface NI;
	public final PcapNetworkInterface PNI;
	
	public NIPMap(NetworkInterface ni, PcapNetworkInterface pni)
	{
		NI = ni;
		PNI = pni;
	}
}
