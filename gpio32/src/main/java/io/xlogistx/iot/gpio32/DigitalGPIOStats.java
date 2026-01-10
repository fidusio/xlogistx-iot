package io.xlogistx.iot.gpio32;

public class DigitalGPIOStats extends GPIOStats<Boolean> {


    protected volatile long lowCounter;
    protected volatile long highCounter;

    public DigitalGPIOStats(GPIOPin pin) {
        super(pin);
        lastState = false;
        startTime = System.currentTimeMillis();
    }

    @Override
    public synchronized void updateStats(Boolean stat) {
        if (stat) {
            highCounter++;
        } else {
            lowCounter++;
        }
        if (lastState != stat) {
            statusChangeCounter++;
        }

        lastTime = System.currentTimeMillis();
        lastState = stat;
    }

    public long getLowCounter() {
        return lowCounter;
    }

    public void setLowCounter(long lowCounter) {
        this.lowCounter = lowCounter;
    }

    public long getHighCounter() {
        return highCounter;
    }

    public void setHighCounter(long highCounter) {
        this.highCounter = highCounter;
    }

    @Override
    public String toString() {
        return "DigitalGPIOStats{" +
                "lowCounter=" + lowCounter +
                ", highCounter=" + highCounter +
                ", statusChangeCounter=" + statusChangeCounter +
                ", startTime=" + startTime +
                ", lastTime=" + lastTime +
                ", lastState=" + lastState +
                ", pin=" + pin +
                '}';
    }
}
