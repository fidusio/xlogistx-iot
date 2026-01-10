package io.xlogistx.iot.gpio;

/**
 * Common interface for GPIO utility operations.
 * Implemented by GPIOTools (Pi4J v1) and GPIO64Tools (Pi4J v3).
 * Uses primitive types to abstract away framework-specific classes.
 */
public interface GPIOHandler {

    /**
     * Reset a pin by its BCM address, releasing any provisioned resources.
     * @param bcmAddress the BCM address of the pin
     */
    void resetPin(int bcmAddress);

    /**
     * Set the PWM range and mode.
     * @param range the PWM range value (typically 2-4095)
     * @param mod the mode value
     */
    void setPWMRangeMod(int range, int mod);

    /**
     * Get the current PWM range value.
     * @return the PWM range
     */
    int getPWMRange();

    /**
     * Convert a duty cycle percentage to a PWM value.
     * @param dutyCycle the duty cycle as a percentage (0-100)
     * @return the corresponding PWM value
     */
    int dutyCycleToPWM(float dutyCycle);

    /**
     * Set an output pin to a specific state.
     * @param bcmAddress the BCM address of the pin
     * @param high true for HIGH state, false for LOW state
     * @param durationInMillis duration before reverting state (0 for permanent)
     */
    void setOutputPin(int bcmAddress, boolean high, long durationInMillis);

    /**
     * Set an output pin state with additional options.
     * @param bcmAddress the BCM address of the pin
     * @param high true for HIGH state, false for LOW state
     * @param persist whether to persist the state on shutdown
     * @param durationInMillis duration in milliseconds
     * @param delay if true, delay before setting; if false, revert after duration
     */
    void setOutputPinState(int bcmAddress, boolean high, boolean persist, long durationInMillis, boolean delay);

    /**
     * Configure a pin as digital input.
     * @param bcmAddress the BCM address of the pin
     */
    void setInputPin(int bcmAddress);

    /**
     * Get the current state of a pin.
     * @param bcmAddress the BCM address of the pin
     * @return true if HIGH, false if LOW
     */
    boolean getPinState(int bcmAddress);

    /**
     * Set up PWM on a pin with specified parameters.
     * @param bcmAddress the BCM address of the pin
     * @param frequency the PWM frequency in Hz
     * @param dutyCycle the duty cycle as a percentage (0-100)
     * @param duration duration in milliseconds (0 for indefinite)
     */
    void setPWM(int bcmAddress, float frequency, float dutyCycle, long duration);

}
