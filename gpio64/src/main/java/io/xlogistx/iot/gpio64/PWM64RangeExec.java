package io.xlogistx.iot.gpio64;

import com.pi4j.io.pwm.Pwm;
import org.zoxweb.shared.data.Range;

/**
 * PWM Range executor for Pi4J v3.
 * Executes PWM sweeps across a range of duty cycle values.
 */
public class PWM64RangeExec implements Runnable {

    private final Pwm pwmOutput;
    private final Range<Integer> dutyCycleRange;
    private final int frequency;
    private final long cycleDelay;
    private int repeat;

    public PWM64RangeExec(Pwm pwmOutput, Range<Integer> dutyCycleRange, int frequency, long cycleDelay, int repeat) {
        this.pwmOutput = pwmOutput;
        this.dutyCycleRange = dutyCycleRange;
        this.frequency = frequency;
        this.cycleDelay = cycleDelay;
        this.repeat = repeat;
    }

    @Override
    public void run() {
        try {
            do {
                // Sweep from start to end
                for (int i = dutyCycleRange.getStart(); i <= dutyCycleRange.getEnd(); i++) {
                    pwmOutput.on(i, frequency);
                    if (cycleDelay > 0) {
                        Thread.sleep(cycleDelay);
                    }
                }

                // Sweep from end back to start
                for (int i = dutyCycleRange.getEnd(); i >= dutyCycleRange.getStart(); i--) {
                    pwmOutput.on(i, frequency);
                    if (cycleDelay > 0) {
                        Thread.sleep(cycleDelay);
                    }
                }

                repeat--;
            } while (repeat > 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
