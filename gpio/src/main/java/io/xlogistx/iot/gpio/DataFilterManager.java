package io.xlogistx.iot.gpio;

import org.zoxweb.shared.filters.DataFilter;
import org.zoxweb.shared.util.RegistrarMap;

import java.util.LinkedHashMap;

public class DataFilterManager
extends RegistrarMap<String, DataFilter, DataFilterManager>
{
    public static final DataFilterManager SINGLETON = new DataFilterManager();

    private DataFilterManager()
    {
        super(new LinkedHashMap<>());
    }


    public DataFilterManager register(DataFilter dataFilter)
    {
        register(dataFilter.getID(), dataFilter);
        return this;
    }
}
