package io.xlogistx.iot.gpio64;

public abstract class GPIO64Stats<T> {


    protected volatile long statusChangeCounter;
    protected volatile long startTime;
    protected volatile long lastTime;


    protected volatile T lastState;
    protected io.xlogistx.iot.data.GPIOBCMPin pin;

    public GPIO64Stats(io.xlogistx.iot.data.GPIOBCMPin pin) {
        this.pin = pin;
    }


    public long getStatusChangeCounter() {
        return statusChangeCounter;
    }

    public void setStatusChangeCounter(long statusChangeCounter) {
        this.statusChangeCounter = statusChangeCounter;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public io.xlogistx.iot.data.GPIOBCMPin getPin() {
        return pin;
    }


    public abstract void updateStats(T stat);

    public T getLastState() {
        return lastState;
    }
}
