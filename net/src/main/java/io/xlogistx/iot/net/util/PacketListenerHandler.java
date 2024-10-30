package io.xlogistx.iot.net.util;

import com.sun.jna.Platform;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.*;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.shared.util.SUS;
import org.zoxweb.shared.util.SharedUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

public abstract class PacketListenerHandler
	implements Runnable, PacketListener, Closeable
{

	protected static Logger log = Logger.getLogger(PacketListenerHandler.class.getName());
	
	protected volatile PcapHandle handle;
	protected volatile Executor executor;
	protected PacketListener packetListener;
	private int count = -1;
	private String filter;
	
	
	public PacketListenerHandler(PcapHandle handle)
	{
		this(handle, null, null);
	}
	
	public PacketListenerHandler(PcapHandle handle, String filter)
	{
		this(handle, filter, null);
	}
	
	public PacketListenerHandler(PcapHandle handle, String filter, Executor executor)
	{
		SharedUtil.checkIfNulls("Handle or PacketListener null", handle);
		this.handle = handle;
		this.filter = filter;
		this.executor = executor;
		packetListener = this;
	}
	
	public void run()
	{
		log.info(Thread.currentThread() + ":Start");
		try 
		{
			if (SUS.isNotEmpty(filter))
			{
				handle.setFilter(filter, BpfCompileMode.OPTIMIZE);
				log.info(Thread.currentThread() + ":Filer set" + filter);
			}
			if (executor != null)
			{
				// start the loop with executor
				log.info(Thread.currentThread() + ":Before loop executor");
				handle.loop(count, packetListener, executor);
				log.info(Thread.currentThread() + ":After loop executor");
			}
			else
			{
				// start the loop executor
				log.info(Thread.currentThread() + ":Before loop NO executor");
				handle.loop(count, packetListener);
				log.info(Thread.currentThread() + ":After loop NO executor");
			}
		} 
		catch (InterruptedException | PcapNativeException | NotOpenException e)
		{
		      e.printStackTrace();
		}
		
		try 
		{
			PcapStat ps = handle.getStats();
			log.info("ps_recv: " + ps.getNumPacketsReceived());
			log.info("ps_drop: " + ps.getNumPacketsDropped());
			log.info("ps_ifdrop: " + ps.getNumPacketsDroppedByIf());
		    if (Platform.isWindows()) {
		    	log.info("bs_capt: " + ps.getNumPacketsCaptured());
		    }

		} catch (PcapNativeException | NotOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("***************************** PacketListener handler end of run method ******************************");
		   
		IOUtil.close(handle);
	}
	
	public void close() throws IOException
	{
		log.info("Closing");
		try {
			handle.breakLoop();
		} catch (NotOpenException e) {
			// TODO Auto-generated catch block
			throw new IOException(e.getMessage());
		}
		
	}
}
