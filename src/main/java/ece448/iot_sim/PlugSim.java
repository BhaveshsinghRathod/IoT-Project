package ece448.iot_sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlugSim { // simulated a smart plug that can be switched on/off and measures power consumption

    public static interface Observer { // Observer interface to notify listeners about plug state and power changes
        void update(String name, String key, String value);
    }

    private final String name; // unique identifier for the plug
    private boolean on = false; // plug state 
    private double power = 0; // power consumption in watts
    private final List<Observer> observers = new ArrayList<>();
    //
    private boolean state;
    private final ConcurrentHashMap<String, Boolean> plugStates = new ConcurrentHashMap<>();


    public PlugSim(String name) {
        this.name = name;
        //
        this.state = false;
        plugStates.put(name, state);
        //
        measurePower(); // measure initial power
    }

    public synchronized void setState(boolean newState) {
        this.state = newState;
        plugStates.put(name, newState);
    }

    public synchronized boolean getStates() {
        return plugStates.getOrDefault(name, false);
    }

    public String getName() {
        return name;
    }

    synchronized public void switchOn() { // switches the plug ON and notifies observer.
        if (!on) {
            on = true;
            measurePower(); // measure immediately after switching ON
            logger.info("Plug {} switched ON", name);
            notifyObservers("state", "on");
        }
    }

    synchronized public void switchOff() { // switches the plug OFF and notifies the observer
        if (on) {
            on = false;
            measurePower(); // measure immediately after switching OFF
            logger.info("Plug {} switched OFF", name);
            notifyObservers("state", "off");
        }
    }

    synchronized public void toggle() { // toggles the plug state between ON and OFF
        if (on) {
            switchOff();
        } else {
            switchOn();
        }
    }

    synchronized public void measurePower() { // Measures and updates power consumption based on plug state
        if (!on) {
            updatePower(0);
            return;
        }

        String[] parts = name.split("\\.");
        if (parts.length == 2) {
            try {
                double newPower = Double.parseDouble(parts[1]);
                updatePower(newPower);
                return;
            } catch (NumberFormatException e) {
                logger.warn("Invalid plug name format: {}", name);
            }
        } else if (power < 100) { // adjust power within a reasonable range for simulation purposes
            updatePower(power + Math.random() * 100);
        } else if (power > 300) {
            updatePower(power - Math.random() * 100);
        } else {
            updatePower(power + Math.random() * 40 - 20);
        }
    }

    protected void updatePower(double p) {
        power = p; // updates the power value and notifies the observer
        logger.debug("Plug {}: power {}", name, power);
        notifyObservers("power", String.format("%.3f", power));
    }

    synchronized public boolean isOn() {
        return on;
    }

    synchronized public double getPower() {
        return power;
    }

    public void addObserver(Observer observer) { // registers an observer and immediately notifies it of the current states and power
        observers.add(observer);
        
        // Immediately notify observer with current state and power
        observer.update(name, "state", isOn() ? "on" : "off");
        observer.update(name, "power", String.format("%.3f", getPower()));
    }

    private void notifyObservers(String key, String value) {
        for (Observer observer : observers) {
            observer.update(name, key, value);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(PlugSim.class);
}