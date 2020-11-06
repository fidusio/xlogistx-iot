package io.xlogistx.iot.device.usb;


import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

public class USBTree {
    /**
     * Dumps the specified device and its sub devices.
     *
     * @param device
     *            The USB device to dump.
     * @param level
     *            The indentation level.
     */
    public static void dump(UsbDevice device, int level)
    {
        for (int i = 0; i < level; i += 1)
            System.out.print("  ");
        System.out.println(device);
        if (device.isUsbHub())
        {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child: (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                dump(child, level + 1);
            }
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            Command-line arguments (Ignored).
     * @throws UsbException
     *             When USB communication fails.
     */
    public static void main(String[] args) throws UsbException
    {

        UsbServices services = UsbHostManager.getUsbServices();
        dump(services.getRootUsbHub(), 0);
    }
}
