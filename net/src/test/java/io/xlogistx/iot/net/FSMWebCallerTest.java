package io.xlogistx.iot.net;


import org.zoxweb.server.fsm.StateInt;
import org.zoxweb.server.fsm.StateMachineInt;
import io.xlogistx.iot.net.data.TaskConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.util.NVBoolean;

import java.io.IOException;

public class FSMWebCallerTest {

    static TaskConfig tc;
    @BeforeAll
    public static void loadConfig() throws IOException {
        String json = IOUtil.inputStreamToString(FSMWebCallerTest.class.getResourceAsStream("/task-config.json"), true);
        tc = GSONUtil.fromJSON(json, TaskConfig.class);
    }

    @Test
    public void testSMWebCaller()
    {
        StateMachineInt<TaskConfig> fsm = FSMWebCaller.create("test", TaskUtil.defaultTaskScheduler(), tc);
        fsm.lookupState(StateInt.States.FINAL).getProperties().add(new NVBoolean(FSMWebCaller.SMWebCaller.SHUTDOWN.getName(), true));
        fsm.start(false);
        System.out.println("After start");
    }


}
