package io.xlogistx.iot.ngpio;

public abstract class NGPIOStats<T> {


    protected volatile long statusChangeCounter;
    protected volatile long startTime;
    protected volatile long lastTime;


    protected volatile T lastState;
    protected NGPIOPin pin;

    public NGPIOStats(NGPIOPin pin) {
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

    public NGPIOPin getPin() {
        return pin;
    }


    public abstract void updateStats(T stat);

    public T getLastState() {
        return lastState;
    }
}
