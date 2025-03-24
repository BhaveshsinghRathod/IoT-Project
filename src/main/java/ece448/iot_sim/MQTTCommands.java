/*package ece448.iot_sim;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.List;

public class MQTTCommands {
    private final List<PlugSim> plugs;
    private final String prefix;

    public MQTTCommands(List<PlugSim> plugs, String prefix) {
        this.plugs = plugs;
        this.prefix = prefix;
    }

    public void handleMessage(String topic, MqttMessage message) {
        String[] parts = topic.split("/");
        if (parts.length != 4 || !parts[0].equals(prefix) || !parts[1].equals("action")) {
            return;
        }

        String plugName = parts[2];
        String action = parts[3];

        PlugSim targetPlug = findPlug(plugName);
        if (targetPlug == null) {
            return;
        }

        switch (action) {
            case "toggle":
                targetPlug.toggle();
                break;
            case "on":
                targetPlug.switchOn();
                break;
            case "off":
                targetPlug.switchOff();
                break;
            default:
                // Invalid action, do nothing
        }
    }

    private PlugSim findPlug(String name) {
        for (PlugSim plug : plugs) {
            if (plug.getName().equals(name)) {
                return plug;
            }
        }
        return null;
    }

    public String getTopic() {
        return prefix + "/action/#";
    }
}
*/



package ece448.iot_sim;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MQTTCommands {

    private final Map<String, PlugSim> plugs = new TreeMap<>();
    private final String topicPrefix;
    private static final Logger logger = LoggerFactory.getLogger(MQTTCommands.class);

    public MQTTCommands(List<PlugSim> plugs, String topicPrefix) {
        if (plugs != null) {
            for (PlugSim plug : plugs) {
                this.plugs.put(plug.getName(), plug);
            }
        }
        this.topicPrefix = topicPrefix != null ? topicPrefix : "";
    }

    public String getTopic() {
        return topicPrefix + "/action/#";
    }

    public void handleMessage(String topic, MqttMessage msg) {
        if (topic == null || msg == null) {
            logger.warn("Null message/topic received");
            return;
        }

        if (topicPrefix.isEmpty()) {
            logger.warn("Empty or null topicPrefix");
            return;
        }

        String[] parts = topic.split("/");
        String[] prefixParts = topicPrefix.split("/");

        if (parts.length < prefixParts.length + 3) {
            logger.warn("Invalid topic format: {}", topic);
            return;
        }

        for (int i = 0; i < prefixParts.length; i++) {
            if (!parts[i].equals(prefixParts[i])) {
                return;
            }
        }

        String plugName = parts[prefixParts.length + 1];
        String action = parts[prefixParts.length + 2];

        PlugSim plug = plugs.get(plugName);
        if (plug == null) {
            logger.warn("Plug not found: {}", plugName);
            return;
        }

        executeAction(plug, action);
    }

    private void executeAction(PlugSim plug, String action) {
        switch (action) {
            case "toggle":
                plug.toggle();
                break;
            case "on":
                plug.switchOn();
                break;
            case "off":
                plug.switchOff();
                break;
            default:
                logger.warn("Unknown action: {}", action);
                return;
        }
        plug.measurePower();
    }
}
