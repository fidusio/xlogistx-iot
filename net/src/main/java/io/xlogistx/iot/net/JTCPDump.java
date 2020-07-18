package io.xlogistx.iot.net;

import java.io.IOException;



import java.util.logging.Logger;


import io.xlogistx.iot.net.util.NIPMap;
import io.xlogistx.iot.net.util.PacketListenerHandler;
import io.xlogistx.iot.net.util.PcapNetUtil;
import org.pcap4j.core.NotOpenException;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;


import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;

import org.pcap4j.packet.Packet;

import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.NifSelector;


public class JTCPDump {
	
	private static Logger log = Logger.getLogger(JTCPDump.class.getName());

  
  private static final int COUNT = -1;
   

  
  private static final int READ_TIMEOUT = 10;

 
  private static final int SNAPLEN =  65536;
  private JTCPDump() {}

  public static void main(String[] args) throws PcapNativeException, NotOpenException, IOException {
	  
	  
	int index  = 0;
	String networkName = args.length > index ? args[index++] : null;
    String filter = args.length > index ? args[index++] : "";

    log.info("COUNT_KEY "+ ": " + COUNT);
    log.info("READ_TIMEOUT_KEY" + ": " + READ_TIMEOUT);
    log.info("SNAPLEN_KEY" + ": " + SNAPLEN);
    log.info("\n");

//    
//    Enumeration<NetworkInterface> eNI = NetworkInterface.getNetworkInterfaces();
//    while(eNI.hasMoreElements())
//    {
//    	NetworkInterface ni = eNI.nextElement();
//    	System.out.println(SharedUtil.toCanonicalID(',', ni.getName(), ni.getDisplayName(), 
//    			ni.getHardwareAddress() != null ?  MACAddressFilter.toString(ni.getHardwareAddress(), ":") : null));
//    }
    
    
   
    
    if(networkName == null)
    {
    	NIPMap[] all = PcapNetUtil.SINGLETON.getNIPMaps();
    	if (all != null && all.length > 0)
    	{
	    	for(NIPMap nipm:all)
	    	{
	    		System.out.println("Usable interface:" + nipm.NI.getName() + ":" + nipm.NI.getDisplayName());
	    	}
	    	System.out.print("Enter network device name:");
	    	networkName = PcapNetUtil.readInput();
    	}
    	
    }
    
    
    
    PcapNetworkInterface nif = null;
    if (networkName !=null)
    {
    	nif = PcapNetUtil.SINGLETON.getPcapNetworkInterface(networkName);
    }
    if (nif == null)
    {
    	nif = new NifSelector().selectNetworkInterface();
    }
    
    if (nif == null) {
      return;
    }
    
    

    System.out.println(nif.getName() + "(" + nif.getDescription() + ")");

    PcapHandle handle
      = nif.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
    
    
//    PacketListenerHandler plh = new  PacketListenerHandler(handle, 
//    		new PacketListener() {
//
//				@Override
//				public void gotPacket(Packet packet) 
//				{
//					// TODO Auto-generated method stub
//					
//				}
//    	
//    });
    
    @SuppressWarnings("resource")
	final PacketListenerHandler plh = new PacketListenerHandler(handle, filter) {

    	long packetCount = 0;
		@Override
		public void gotPacket(Packet packet) 
		{
			packetCount++;
			// TODO Auto-generated method stub
			//log.info(""+packetCount);
//			if (packet instanceof ArpPacket)
//			{
//				log.info(""+packet);
//				log.info(""+packetCount);
//			}
			
			//if (packet instanceof EthernetPacket)
			try
			{
			
				EthernetPacket ep = (EthernetPacket) packet;
				
				
				if (ep.getHeader().getType() == EtherType.ARP)
            	{
            		
            		System.out.println("{ packetCount:" + packetCount);
            		ArpPacket ap = (ArpPacket)ep.getPayload();
            		//System.out.println(ep.getPayload());
            		//System.out.println(p != null ? p.getClass().getName() : "");
            		System.out.println(handle.getTimestamp());
            		//ArpPacket ap = ArpPacket.newPacket(packet, 0, packet.length);
            		//System.out.println(ep.getHeader());
            		//ArpPacket ap;
					
						//ap = ArpPacket.newPacket(ep.getRawData(), 0, ep.getRawData().length);
						//System.out.println(ap.getHeader());
	            		
	            		System.out.println(ap.getHeader().getSrcHardwareAddr()+ "-" + ap.getHeader().getSrcProtocolAddr().getHostAddress());
	            		//System.out.println(ap.getHeader().getDstHardwareAddr()+ "-" + ap.getHeader().getDstProtocolAddr().getHostAddress());
					
					System.out.println("}");
            		
            	}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			//System.out.println(Thread.currentThread().getName());
		}
    };
    
    plh.run();
    
  

//    RawPacketListener listener
//      = new RawPacketListener() {
//          @Override
//          public void gotPacket(byte[] packet) {
//            
//            //System.out.println(ByteArrays.toHexString(packet, " "));
//            //System.out.println(SharedStringUtil.bytesToHex(packet));
//            try {
//            	EthernetPacket ep = EthernetPacket.newPacket(packet, 0, packet.length);
//            	
//            	
//            	if (ep.getHeader().getType() == EtherType.ARP)
//            	{
//            		Packet p = ep.getOuterOf(ArpPacket.class);
//            		System.out.println("{ size:" + packet.length);
//            		System.out.println(p != null ? p.getClass().getName() : "");
//            		System.out.println(handle.getTimestamp());
//            		//ArpPacket ap = ArpPacket.newPacket(packet, 0, packet.length);
//            		System.out.println(ep.getHeader());
//            		ArpPacket ap = ArpPacket.newPacket(packet, 0, packet.length);
//            		System.out.println(ap.getHeader());
//            		//System.out.println(ep.getHeader().getSrcAddr() + "   " + ep.getHeader().getDstAddr());
//            		//System.out.println(ap.getHeader().getSrcHardwareAddr()+ ":" + ap.getHeader().getSrcProtocolAddr());
//            		System.out.println("}");
//            	}
//            	//System.out.println(ep.getHeader().getType());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//           
//            
//          }
//        };
        
  
  }

}
