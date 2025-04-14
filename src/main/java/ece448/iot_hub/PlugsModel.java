package ece448.iot_hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class PlugsModel {

    private final MqttController mqttController;

    public PlugsModel(MqttController mqttController) {
        this.mqttController = mqttController;
    }

    /**
     * Creates a representation of a plug with its name, state, and power.
     *
     * @param plugName The name of the plug.
     * @return A map containing the plug's details.
     */
    synchronized public Map<String, Object> createPlug(String plugName) {
        Map<String, Object> plugDetails = new HashMap<>();
        plugDetails.put("name", plugName);

        String state = mqttController.getState(plugName);
        plugDetails.put("state", (state != null) ? state : "off");

        String power = mqttController.getPower(plugName);
        plugDetails.put("power", (power != null) ? Float.parseFloat(power) : 0.0);

        return plugDetails;
    }

    /**
     * Retrieves a list of all plugs with their details.
     *
     * @return A list of maps, each representing a plug.
     */
    synchronized public List<Map<String, Object>> getAllPlugs() {
        List<Map<String, Object>> plugsList = new ArrayList<>();

        // Collect all unique plug names from states and powers
        Set<String> uniquePlugNames = new HashSet<>();
        uniquePlugNames.addAll(mqttController.getStates().keySet());
        uniquePlugNames.addAll(mqttController.getPowers().keySet());

        for (String plugName : uniquePlugNames) {
            plugsList.add(createPlug(plugName));
        }

        return plugsList;
    }

    /**
     * Sends a control action to a specific plug.
     *
     * @param plugName The name of the plug to control.
     * @param action   The action to perform (e.g., "on", "off").
     */
    synchronized public void controlPlug(String plugName, String action) {
        mqttController.publishAction(plugName, action);
    }
}
