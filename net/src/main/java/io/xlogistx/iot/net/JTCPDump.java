package io.xlogistx.iot.net;

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
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.NifSelector;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.shared.util.SharedUtil;

import java.io.IOException;


public class JTCPDump {
	public static LogWrapper log = new LogWrapper(JTCPDump.class).setEnabled(true);

	private static final int COUNT = -1;
   

  
	private static final int READ_TIMEOUT = 10;


	private static final int SNAPLEN =  65536;
	private JTCPDump() {}

	public static void main(String[] args) throws PcapNativeException, NotOpenException, IOException {


	int index  = 0;
	String networkName = args.length > index ? args[index++] : null;
	String filter = args.length > index ? args[index++] : "";

	if(log.isEnabled()) log.getLogger().info("COUNT_KEY "+ ": " + COUNT);
	if(log.isEnabled()) log.getLogger().info("READ_TIMEOUT_KEY" + ": " + READ_TIMEOUT);
	if(log.isEnabled()) log.getLogger().info("SNAPLEN_KEY" + ": " + SNAPLEN);
	if(log.isEnabled()) log.getLogger().info("\n");

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
			StringBuilder sb = new StringBuilder();
			System.out.println();
			System.out.println("Usable network interfaces:");
			for(NIPMap nipm:all)
			{
				if(sb.length() > 0){
					sb.append(", ");
				}
				sb.append(nipm.NI.getName());
				System.out.println("* Network name " + nipm.NI.getName() + ", description:" + nipm.NI.getDisplayName());
			}
			System.out.print("\n\nSelect a network " + sb + " :");
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
			//if(log.isEnabled()) log.getLogger().info(""+packetCount);
	//			if (packet instanceof ArpPacket)
	//			{
	//				if(log.isEnabled()) log.getLogger().info(""+packet);
	//				if(log.isEnabled()) log.getLogger().info(""+packetCount);
	//			}

			//if (packet instanceof EthernetPacket)
			try
			{

				EthernetPacket ep = (EthernetPacket) packet;

				EtherType et = ep.getHeader().getType();
				if (EtherType.ARP == et)
				{

					ArpPacket ap = (ArpPacket)ep.getPayload();
					//System.out.println(ep.getPayload());
					//System.out.println(p != null ? p.getClass().getName() : "");

					//ArpPacket ap = ArpPacket.newPacket(packet, 0, packet.length);
					//System.out.println(ep.getHeader());
					//ArpPacket ap;

						//ap = ArpPacket.newPacket(ep.getRawData(), 0, ep.getRawData().length);
						//System.out.println(ap.getHeader());

						System.out.println(packetCount + "," +handle.getTimestamp() + "," + ap.getHeader().getSrcHardwareAddr()+ "->" + ap.getHeader().getSrcProtocolAddr().getHostAddress());
						//System.out.println(ap.getHeader().getDstHardwareAddr()+ "-" + ap.getHeader().getDstProtocolAddr().getHostAddress());



				}
				else if (et == EtherType.IPV4)
				{
					IpV4Packet v4Packet = (IpV4Packet)ep.getPayload();

					//if(log.isEnabled()) log.getLogger().info(ep.getHeader().getSrcAddr() + "->" + ep.getHeader().getDstAddr())
					//if(log.isEnabled()) log.getLogger().info("{ packetCount:" + packetCount+ "\n" + v4Packet.getHeader() + "\n]" );
					System.out.println(SharedUtil.toCanonicalID(',', packetCount, handle.getTimestamp(),"S-IPV4:"+v4Packet.getHeader().getSrcAddr(), ep.getHeader().getSrcAddr(),
							"D-IPV4:"+v4Packet.getHeader().getDstAddr(), ep.getHeader().getDstAddr(), ep.getHeader().getType()));

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
