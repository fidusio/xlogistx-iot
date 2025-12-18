package io.xlogistx.iot.data;

import org.zoxweb.shared.filters.DataFilter;
import org.zoxweb.shared.util.ValueWithUnit;

public class MultiplierDataFilter
        extends DataFilter<Integer, ValueWithUnit<Float, String>, String> {

    private float multiplier = 1.0f;
    private String unit;

    public MultiplierDataFilter(String type, String id, String name, String description) {
        super(type, id, name, description);
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public ValueWithUnit<Float, String> decode(Integer input) {
        float val = input * multiplier;

        ValueWithUnit<Float, String> ret = new ValueWithUnit<Float, String>(val, unit);
        ret.setName(getName());
        return ret;
    }
}
