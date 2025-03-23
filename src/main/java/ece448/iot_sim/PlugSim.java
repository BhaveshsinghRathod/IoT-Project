package ece448.iot_sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simulate a smart plug with power monitoring.
 */
public class PlugSim {

    private final String name;
    private boolean on = false;
    private double power = 0; // in watts
    private List<Consumer<Boolean>> stateChangeListeners = new ArrayList<>();
    private List<Consumer<Double>> powerChangeListeners = new ArrayList<>();

    public PlugSim(String name) {
        this.name = name;
    }

    /**
     * No need to synchronize if read a final field.
     */
    public String getName() {
        return name;
    }

    /**
     * Switch the plug on.
     */
    synchronized public void switchOn() {
        if (!on) {
            on = true;
            notifyStateChange();
        }
    }

    /**
     * Switch the plug off.
     */
    synchronized public void switchOff() {
        if (on) {
            on = false;
            updatePower(0);
            notifyStateChange();
        }
    }

    /**
     * Toggle the plug.
     */
    synchronized public void toggle() {
        on = !on;
        if (!on) {
            updatePower(0);
        }
        notifyStateChange();
    }

    /**
     * Measure power.
     */
    synchronized public void measurePower() {
        if (!on) {
            updatePower(0);
            return;
        }

        // a trick to help testing
        if (name.indexOf(".") != -1) {
            try {
                updatePower(Integer.parseInt(name.split("\\.")[1]));
            } catch (NumberFormatException e) {
                logger.warn("Invalid power value in plug name: {}", name);
            }
        }
        // do some random walk
        else if (power < 100) {
            updatePower(power + Math.random() * 100);
        } else if (power > 300) {
            updatePower(power - Math.random() * 100);
        } else {
            updatePower(power + Math.random() * 40 - 20);
        }
    }

    protected void updatePower(double p) {
        if (power != p) {
            power = p;
            logger.debug("Plug {}: power {}", name, power);
            notifyPowerChange();
        }
    }

    /**
     * Getter: current state
     */
    synchronized public boolean isOn() {
        return on;
    }

    /**
     * Getter: last power reading
     */
    synchronized public double getPower() {
        return power;
    }

    public void addStateChangeListener(Consumer<Boolean> listener) {
        stateChangeListeners.add(listener);
    }

    public void addPowerChangeListener(Consumer<Double> listener) {
        powerChangeListeners.add(listener);
    }

    private void notifyStateChange() {
        for (Consumer<Boolean> listener : stateChangeListeners) {
            listener.accept(on);
        }
    }

    private void notifyPowerChange() {
        for (Consumer<Double> listener : powerChangeListeners) {
            listener.accept(power);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(PlugSim.class);
}
