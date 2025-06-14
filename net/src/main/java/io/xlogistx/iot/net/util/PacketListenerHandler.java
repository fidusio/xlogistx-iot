package io.xlogistx.iot.net.util;

import com.sun.jna.Platform;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.*;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.shared.util.CloseableType;
import org.zoxweb.shared.util.SUS;
import org.zoxweb.shared.util.SharedUtil;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PacketListenerHandler
        implements Runnable, PacketListener, CloseableType {

    protected static LogWrapper log = new LogWrapper(PacketListenerHandler.class).setEnabled(true);

    protected volatile PcapHandle handle;
    protected volatile Executor executor;
    protected PacketListener packetListener;
    //private int count = -1;
    private final String filter;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);// = new AtomicBoolean();


    public PacketListenerHandler(PcapHandle handle) {
        this(handle, null, null);
    }

    public PacketListenerHandler(PcapHandle handle, String filter) {
        this(handle, filter, null);
    }

    public PacketListenerHandler(PcapHandle handle, String filter, Executor executor) {
        SharedUtil.checkIfNulls("Handle or PacketListener null", handle);
        this.handle = handle;
        this.filter = filter;
        this.executor = executor;
        packetListener = this;
    }

    public void run() {
        if (log.isEnabled()) log.getLogger().info(Thread.currentThread() + ":Start");
        try {
            if (SUS.isNotEmpty(filter)) {
                handle.setFilter(filter, BpfCompileMode.OPTIMIZE);
                if (log.isEnabled()) log.getLogger().info(Thread.currentThread() + ":Filer set" + filter);
            }
            if (executor != null) {
                // start the loop with executor
                if (log.isEnabled()) log.getLogger().info(Thread.currentThread() + ":Before loop executor");
                handle.loop(-1, packetListener, executor);
                if (log.isEnabled()) log.getLogger().info(Thread.currentThread() + ":After loop executor");
            } else {
                // start the loop executor
                if (log.isEnabled()) log.getLogger().info(Thread.currentThread() + ":Before loop NO executor");
                handle.loop(-1, packetListener);
                if (log.isEnabled()) log.getLogger().info(Thread.currentThread() + ":After loop NO executor");
            }
        } catch (InterruptedException | PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }

        try {
            PcapStat ps = handle.getStats();
            if (log.isEnabled()) log.getLogger().info("ps_recv: " + ps.getNumPacketsReceived());
            if (log.isEnabled()) log.getLogger().info("ps_drop: " + ps.getNumPacketsDropped());
            if (log.isEnabled()) log.getLogger().info("ps_ifdrop: " + ps.getNumPacketsDroppedByIf());
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


    public boolean isClosed() {
        return !handle.isOpen();
    }

    public void close() throws IOException {
        if (!isClosed.getAndSet(true)) {
            log.getLogger().info("Closing");
            IOUtil.close(handle);
            try {
                handle.breakLoop();
            } catch (NotOpenException e) {
                // TODO Auto-generated catch block
                throw new IOException(e.getMessage());
            }
        }

    }
}
