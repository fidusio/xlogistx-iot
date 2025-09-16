package io.xlogistx.iot.net;

import io.xlogistx.iot.net.data.NIRenameConfig;
import io.xlogistx.iot.net.data.OSConfig;
import org.zoxweb.server.util.RuntimeUtil;
import org.zoxweb.shared.util.SUS;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.logging.Logger;

public class NIRename {

    private static final  Logger log = Logger.getLogger(NIRename.class.getName());

    private NIRename() {
    }

    public static NIRenameConfig renameNI(OSConfig oscd, NIRenameConfig nirc, long upDelay) throws IOException, InterruptedException {
        SUS.checkIfNulls("Null nird or parameters", oscd, nirc, nirc.getNIToName(), nirc.getSysNetFolder());
        return renameNI(oscd.getIfUpCommand(), nirc, upDelay);
    }


    public static NIRenameConfig renameNI(String niUpCommand, NIRenameConfig nirc, long upDelay) throws IOException, InterruptedException {
        SUS.checkIfNulls("Null nird or parameters", niUpCommand, nirc, nirc.getNIToName(), nirc.getSysNetFolder());
        NetworkInterface niToLocate = NetworkInterface.getByName(nirc.getNIToName());
        if (niToLocate == null) {
            File folder = new File(nirc.getSysNetFolder());
            if (!folder.isDirectory())
                throw new IllegalArgumentException(nirc.getSysNetFolder() + " is not a folder.");
            for (File f : folder.listFiles()) {
                //log.info("file:" + f.getName() + " isFile " + f.isFile() + " isDirectory " + f.isDirectory());
                if (f.isDirectory()) {
                    if (!nirc.getFilteredNIs().contains(f.getName())) {
                        nirc.setNIToRename(f.getName());
                        log.info("to rename:" + f);
                        break;
                    }
                }
            }

            if (nirc.getNIToRename() != null) {
                log.info("" + RuntimeUtil.runAndFinish(nirc.getScript(), nirc.getNIToRename(), nirc.getNIToName()));
                if (upDelay > 0) {
                    try {
                        Thread.sleep(upDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("" + RuntimeUtil.runAndFinish(niUpCommand, nirc.getNIToName()));
                if (upDelay > 0) {
                    try {
                        Thread.sleep(upDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return nirc;
    }
}
