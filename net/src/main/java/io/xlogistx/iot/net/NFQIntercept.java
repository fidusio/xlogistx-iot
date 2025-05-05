package io.xlogistx.iot.net;

import io.xlogistx.iot.net.iptables.NetFilterByAddress;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.byref.PointerByReference;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.CloseableType;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.ParamUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/** Java ↔ NFQUEUE demo — IPv4 + TCP, two queues (100/101). */
public final class NFQIntercept
    implements Runnable, CloseableType
{


    /*=========================Class Variables ======================================================*/
    public static final LogWrapper log = new LogWrapper(NFQIntercept.class).setEnabled(true);
    private final int incomingQueueNum;
    private final int outgoingQueueNum;
    private final NetFilterByAddress netFilter;
    private boolean alive = true;

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close()
    {
        alive = false;
    }

    /**
     * Checks if closed.
     *
     * @return true if closed
     */
    @Override
    public boolean isClosed() {
        return !alive;
    }


    /*===============================================================================================*/

    /** libnetfilter_queue symbols */
    public interface LibNFQ {
        NetfilterBridge.LibNFQ I = LibraryLoader.create(NetfilterBridge.LibNFQ.class).load("netfilter_queue");

        Pointer nfq_open();
        int     nfq_unbind_pf(Pointer h, int proto);
        int     nfq_bind_pf  (Pointer h, int proto);

        Pointer nfq_create_queue(Pointer h, int num,
                                 NetfilterBridge.PacketHandler cb, Pointer userData);
        int     nfq_destroy_queue(Pointer qh);

        int nfq_set_mode(Pointer qh, int mode, int range);
        int nfq_fd(Pointer h);

        int     nfq_handle_packet(Pointer h, Pointer buf, int len);
        int     nfq_set_verdict(Pointer qh, int id, int verdict,
                                int dataLen, Pointer buf);

        Pointer nfq_get_msg_packet_hdr(Pointer nfad);
        int     nfq_get_payload(Pointer nfad, PointerByReference dataPtr);
    }

    /** Callback interface: jnr-ffi stub via @Delegate */
    public interface PacketHandler {
        @Delegate
        int invoke(Pointer qh, Pointer nfmsg,
                   Pointer nfadata, Pointer userData);
    }

    /** libc read(2) + poll(2) */
    public interface LibC {
        NetfilterBridge.LibC I = LibraryLoader.create(NetfilterBridge.LibC.class).load("c");
        int read(int fd, byte[] buf, int len);
        final class PollFD extends Struct {
            public Signed32   fd      = new Signed32();
            public Unsigned16 events  = new Unsigned16();
            public Unsigned16 revents = new Unsigned16();
            public PollFD(jnr.ffi.Runtime r) { super(r); }
        }
        int poll(NetfilterBridge.LibC.PollFD fds, int nfds, int timeoutMs);
    }

    // constants & one‐off allocations
    private static final byte[] srcHost = new byte[4];
    private static final byte[] dstHost = new byte[4];


    private static final int AF_INET   = 2;
    private static final int NF_ACCEPT = 1;
    private static final int NF_DROP   = 0;
    private static final int NFCOPY    = 2;
    private static final jnr.ffi.Runtime RUNTIME = jnr.ffi.Runtime.getSystemRuntime();
    private static final Pointer EMPTY   = Pointer.wrap(RUNTIME, ByteBuffer.allocate(0));

    /** Extract packet ID (network-order → host-order) */
    private static int packetId(Pointer nfad, NetfilterBridge.LibNFQ nfq) {
        Pointer hdr = nfq.nfq_get_msg_packet_hdr(nfad);
        int be = (hdr == null ? 0 : hdr.getInt(0));
        return Integer.reverseBytes(be);  // ntohl() :contentReference[oaicite:0]{index=0}
    }

    /** Pull pointer to L3+ payload */
    private static Pointer payloadPtr(Pointer nfad, NetfilterBridge.LibNFQ nfq) {
        PointerByReference ref = new PointerByReference();
        return (nfq.nfq_get_payload(nfad, ref) <= 0)
                ? null : ref.getValue();
    }


    private static byte[] srcAddrAsBytes(Pointer nfad, NetfilterBridge.LibNFQ nfq)
    {
        Pointer p = payloadPtr(nfad, nfq);
        if (p == null) return  InetAddress.getLoopbackAddress().getAddress();

        p.get(12, srcHost, 0, srcHost.length);
        return srcHost;
    }

    private static byte[] dstAddrAsBytes(Pointer nfad, NetfilterBridge.LibNFQ nfq)
    {
        Pointer p = payloadPtr(nfad, nfq);
        if (p == null) return  InetAddress.getLoopbackAddress().getAddress();

        p.get(16, dstHost, 0, dstHost.length);
        return dstHost;
    }

    private static int dstPort(Pointer nfad, NetfilterBridge.LibNFQ nfq) {
        Pointer p = payloadPtr(nfad, nfq);
        if (p == null) return -1;
        int ihl = (p.getByte(0) & 0x0F) * 4;
        return p.getShort(ihl + 2) & 0xFFFF;
    }






    public NFQIntercept(NetFilterByAddress netFilter, int incomingQueueNum, int outgoingQueueNum)
    {
        this.incomingQueueNum = incomingQueueNum;
        this.outgoingQueueNum = outgoingQueueNum;
        this.netFilter = netFilter;
    }

    public void run()
    {
        try {
            NetfilterBridge.LibNFQ nfq = NetfilterBridge.LibNFQ.I;
            Pointer h = nfq.nfq_open();
            if (h == null) throw new IllegalStateException("nfq_open failed");

            nfq.nfq_unbind_pf(h, AF_INET);
            nfq.nfq_bind_pf(h, AF_INET);

            /* 2 separate handlers: one for 100, one for 101 */
            NetfilterBridge.PacketHandler hIn = (qh, msg, nfad, ud) -> {
                int id = packetId(nfad, nfq);

                boolean ok = true;
                try {
                    byte[] src = srcAddrAsBytes(nfad, nfq);
                    int dport = dstPort(nfad, nfq);
                    ok = netFilter.validateIncoming(src, dport);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return nfq.nfq_set_verdict(qh, id,
                        ok ? NF_ACCEPT : NF_DROP,
                        0, EMPTY);
            };
            NetfilterBridge.PacketHandler hOut = (qh, msg, nfad, ud) -> {
                int id = packetId(nfad, nfq);
                //System.out.println("packetid: " + id);
                boolean ok = true;
                try {
                    byte[] dst = dstAddrAsBytes(nfad, nfq);
                    int dport = dstPort(nfad, nfq);
                    //System.out.println("host: " + dst + "  port: " + dport);
                    ok = netFilter.validateOutgoing(dst, dport);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return nfq.nfq_set_verdict(qh, id,
                        ok ? NF_ACCEPT : NF_DROP,
                        0, EMPTY);
            };

            Pointer qIn = nfq.nfq_create_queue(h, incomingQueueNum, hIn, EMPTY);
            Pointer qOut = nfq.nfq_create_queue(h, outgoingQueueNum, hOut, EMPTY);
            if (qIn == null || qOut == null)
                throw new IllegalStateException("nfq_create_queue failed");

            nfq.nfq_set_mode(qIn, NFCOPY, 0xffff);
            nfq.nfq_set_mode(qOut, NFCOPY, 0xffff);

            /* one reusable buffer + pointer */
            byte[] buf = new byte[65_535];
            ByteBuffer bb = ByteBuffer.wrap(buf);
            Pointer pkt = Pointer.wrap(RUNTIME, bb);

            /* poll/read loop */
            int fd = nfq.nfq_fd(h);
            NetfilterBridge.LibC.PollFD pfd = new NetfilterBridge.LibC.PollFD(RUNTIME);
            pfd.fd.set(fd);
            pfd.events.set((short) 1);

            while (alive) {
                if (NetfilterBridge.LibC.I.poll(pfd, 1, 1000) > 0) {
                    int n = NetfilterBridge.LibC.I.read(fd, buf, buf.length);
                    if (n > 0)
                        nfq.nfq_handle_packet(h, pkt, n);
                }
            }


            try {
                System.err.println("Shutting down NFQUEUE...");
                if (qIn != null)  nfq.nfq_destroy_queue(qIn);
                if (qOut != null) nfq.nfq_destroy_queue(qOut);
                // optional: unbind
                nfq.nfq_unbind_pf(h, AF_INET);
                // finally close the handle
                nfq.nfq_close(h);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            close();
        }
    }





    public static void main(String[] args)
    {
        try
        {
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            System.out.println(params);
            int incomingQueueId = params.intValue("in-queue");
            int outgoingQueueId = params.intValue("out-queue");
            boolean outRule = params.booleanValue("out-rule");
            boolean inRule = params.booleanValue("in-rule");
            String inHost = params.stringValue("in-host");
            String outHost = params.stringValue("out-host");

            String timeToLive = params.stringValue("ttl", true);


            log.setEnabled(params.booleanValue("dbg", true));

            NetFilterByAddress filter = new NetFilterByAddress(inRule, outRule);
            filter.addIncomingHost(inHost);
            filter.addOutgoingHost(outHost);



            NFQIntercept intercept = new NFQIntercept(filter, incomingQueueId, outgoingQueueId);
            new Thread(intercept).start();
            long ttl = 0;
            if (timeToLive != null)
            {
                try
                {
                    ttl = Const.TimeInMillis.toMillisNullZero(timeToLive);
                }
                catch (Exception e)
                {

                }
            }
            if(ttl > 0 )
            {
                TaskUtil.defaultTaskScheduler().queue(ttl,()->intercept.close());
                TaskUtil.waitIfBusyThenClose(Const.TimeInMillis.SECOND.MILLIS);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Usage NetfilterIntercept: in-queue=netfilter-queue-number out-queue=netfilter-queue-number in-host=host out-host=host in-rule=[false or true] out-rule=[false or true] [dbg=true] [ttl=1:00:00 (run for one hour)]");
        }

    }
}
