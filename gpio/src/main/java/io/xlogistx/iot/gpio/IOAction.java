package io.xlogistx.iot.gpio;

import org.zoxweb.shared.util.GetValue;
import org.zoxweb.shared.util.SharedUtil;

public enum IOAction implements GetValue<String> {
    READ("-r"),
    READ_AS_INPUT("-ri"),
    MONITOR("-m"),
    FLOW("-f"),
    SET("-s"),
    STATE_MONITOR("-sm"),
    PROVISION("-p"),
    // pulse width modulation
    PWM("-pwm"),
    SET_PULL_UP("-spu"),
    SET_PULL_DOWN("-spd");

    private final String value;

    IOAction(String val) {
        value = val;
    }

    @Override
    public String getValue() {
        return value;
    }

    public static IOAction lookup(String token) {
        return SharedUtil.lookupEnum(token, values());
    }
}
