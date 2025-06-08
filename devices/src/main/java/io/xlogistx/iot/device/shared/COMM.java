package io.xlogistx.iot.device.shared;

import org.zoxweb.shared.data.PropertyDAO;
import org.zoxweb.shared.util.*;

public class COMM
        extends PropertyDAO {


    public enum Param
            implements GetNVConfig {
        PROTOCOL(NVConfigManager.createNVConfig("proto", "Protocol", "Protocol", false, true, IOTConst.Protocol.class)),
        FREQ(NVConfigManager.createNVConfig("freq", "Frequency", "Frequency", false, true, double.class)),
        FREQ_UNIT(NVConfigManager.createNVConfig("freq_unit", "Frequency unit", "Frequency", false, true, IOTConst.FrequencyUnit.class)),
        ;
        private final NVConfig nvc;

        Param(NVConfig nvc) {
            this.nvc = nvc;
        }

        public NVConfig getNVConfig() {
            return nvc;
        }
    }

    public static final NVConfigEntity NVC_COMM = new NVConfigEntityLocal("comm",
            null,
            "COMM",
            true,
            false,
            false,
            false,
            COMM.class,
            SharedUtil.extractNVConfigs(Param.values()),
            null,
            false,
            PropertyDAO.NVC_PROPERTY_DAO);

    public COMM() {
        super(NVC_COMM);
    }

    public COMM(IOTConst.Protocol protocol, double frequency, IOTConst.FrequencyUnit unit) {
        this();
        setProtocol(protocol);
        setFrequency(frequency);
        setFrequencyUnit(unit);
    }

    public IOTConst.Protocol getProtocol() {
        return lookupValue(Param.PROTOCOL);
    }

    public void setProtocol(IOTConst.Protocol protocol) {
        setValue(Param.PROTOCOL, protocol);
    }

    public double getFrequency() {
        return lookupValue(Param.FREQ);
    }

    public void setFrequency(double speed) {
        setValue(Param.FREQ, speed);
    }

    public IOTConst.FrequencyUnit getFrequencyUnit() {
        return lookupValue(Param.FREQ_UNIT);
    }

    public void setFrequencyUnit(IOTConst.FrequencyUnit freqUnit) {
        setValue(Param.FREQ_UNIT, freqUnit);
    }


}
