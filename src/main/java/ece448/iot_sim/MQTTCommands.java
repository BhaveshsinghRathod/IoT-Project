package ece448.iot_sim;

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
