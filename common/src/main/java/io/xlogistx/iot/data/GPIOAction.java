package io.xlogistx.iot.data;

import org.zoxweb.shared.util.GetValue;
import org.zoxweb.shared.util.SharedUtil;

public enum GPIOAction implements GetValue<String> {
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

    GPIOAction(String val) {
        value = val;
    }

    @Override
    public String getValue() {
        return value;
    }

    public static GPIOAction lookup(String token) {
        return SharedUtil.lookupEnum(token, values());
    }
}
