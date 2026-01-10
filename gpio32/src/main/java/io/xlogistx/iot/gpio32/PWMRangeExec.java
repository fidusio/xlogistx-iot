package io.xlogistx.iot.gpio32;

import com.pi4j.io.gpio.GpioPinPwmOutput;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.data.Range;
import org.zoxweb.shared.util.Const;

import java.util.logging.Logger;

public class PWMRangeExec
        implements Runnable {

    private static final Logger log = Logger.getLogger(PWMRangeExec.class.getName());
    private Range<Integer> range;
    private long delay;
    private int count;
    private GpioPinPwmOutput pwmPin;

    //    private int currentPWM;
    public PWMRangeExec(GpioPinPwmOutput pwmPin, Range<Integer> range, long delay, int count) {
        this.range = range;
        this.delay = delay;
        this.count = count;
        this.pwmPin = pwmPin;
//        currentPWM = range.getStart();
    }

    @Override
    public void run() {
//        currentPWM++;
//        if(!range.contains(currentPWM))
//        {
//            currentPWM = range.getStart();
//            count--;
//        }
        log.info("count:" + count + " range:" + range + " delay:" + Const.TimeInMillis.toString(delay));

        for (int i = range.getStart(); i < range.getEnd(); i++) {
            pwmPin.setPwm(i);
            TaskUtil.sleep(delay);
        }
        count--;
        if (count > 0) {
            TaskUtil.defaultTaskScheduler().queue(delay, this);
        } else {
            pwmPin.setPwm(0);
        }

    }
}
