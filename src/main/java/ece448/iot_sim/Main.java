/*package ece448.iot_sim;

import java.io.File;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ece448.iot_sim.http_server.JHTTP;

public class Main implements AutoCloseable {
    public static void main(String[] args) throws Exception {
        String configFile = args.length > 0 ? args[0] : "simConfig.json";
        SimConfig config = mapper.readValue(new File(configFile), SimConfig.class);
        logger.info("Loaded config: {}", mapper.writeValueAsString(config));

        try (Main m = new Main(config)) {
            while (true) Thread.sleep(60000);
        }
    }

    public Main(SimConfig config) throws Exception {
        // Initialize plugs
        ArrayList<PlugSim> plugs = new ArrayList<>();
        for (String name : config.getPlugNames()) {
            plugs.add(new PlugSim(name));
        }

        // Start power monitoring
        new MeasurePower(plugs).start();

        // HTTP Server
        this.http = new JHTTP(config.getHttpPort(), new HTTPCommands(plugs));
        this.http.start();

        // MQTT Client Setup
        this.mqttClient = new MqttClient(
            config.getMqttBroker(),
            config.getMqttClientId(),
            new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence()
        );
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.connect(options);

        // MQTT Command Handling
        MQTTCommands commands = new MQTTCommands(plugs, config.getMqttTopicPrefix());
        mqttClient.subscribe(commands.getTopic(), (topic, msg) -> {
            try {
                commands.handleMessage(topic, msg);
            } catch (Exception e) {
                logger.error("MQTT command error: {}", e.getMessage());
            }
        });

        // MQTT Update Publishing
        MQTTUpdates updates = new MQTTUpdates(config.getMqttTopicPrefix());
        for (PlugSim plug : plugs) {
            // State updates
            plug.addStateChangeListener(state -> {
                try {
                    String topic = updates.getTopic(plug.getName(), "state");
                    MqttMessage message = updates.getMessage(state ? "on" : "off");
                    mqttClient.publish(topic, message);
                } catch (Exception e) {
                    logger.error("State update failed: {}", e.getMessage());
                }
            });

            // Power updates (always format to 3 decimal places)
            plug.addPowerChangeListener(power -> {
                try {
                    String topic = updates.getTopic(plug.getName(), "power");
                    String value = String.format("%.3f", power);
                    MqttMessage message = updates.getMessage(value);
                    mqttClient.publish(topic, message);
                } catch (Exception e) {
                    logger.error("Power update failed: {}", e.getMessage());
                }
            });
        }
    }

    @Override
    public void close() throws Exception {
        http.close();
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
            mqttClient.close();
        }
    }

    private final JHTTP http;
    private final MqttClient mqttClient;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
}
*/


package ece448.iot_sim;

import java.io.File;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ece448.iot_sim.http_server.JHTTP;

public class Main implements AutoCloseable {

    private final JHTTP http; // HTTP server instance
    private final MqttClient mqtt; // MQTT client instance

    private static final ObjectMapper mapper = new ObjectMapper(); // JSON object mapper for reading configuration
    private static final Logger logger = LoggerFactory.getLogger(Main.class); // Logger instance for logging important events

    public static void main(String[] args) throws Exception {
        String configFile = args.length > 0 ? args[0] : "simConfig.json"; // Load configuration file, defaulting to "simconfig.json" if no arguments are provided
        SimConfig config = mapper.readValue(new File(configFile), SimConfig.class);
        logger.info("{}: {}", configFile, mapper.writeValueAsString(config));

        try (Main m = new Main(config)) {
            for (;;) {
                Thread.sleep(60000);
            }
        }
    }

    public Main(SimConfig config) throws Exception {
        ArrayList<PlugSim> plugs = new ArrayList<>(); // create a list to store simulated plugs
        for (String plugName : config.getPlugNames()) {
            plugs.add(new PlugSim(plugName));
        }

        // start monitoring power consumption of the plugs
        MeasurePower measurePower = new MeasurePower(plugs);
        measurePower.start();

        // initialize and start the HTTP server for handling commands
        this.http = new JHTTP(config.getHttpPort(), new HTTPCommands(plugs));
        this.http.start();

        // initialize and connect the MQTT client
        this.mqtt = new MqttClient(config.getMqttBroker(), config.getMqttClientId(), new MemoryPersistence());
        this.mqtt.connect();

        // setup MQTT commands to handle incoming messages
        MQTTCommands mqttCmd = new MQTTCommands(plugs, config.getMqttTopicPrefix());
        logger.info("Mqtt subscribe to {}", mqttCmd.getTopic());
        this.mqtt.subscribe(mqttCmd.getTopic(), (topic, msg) -> {
            mqttCmd.handleMessage(topic, msg);
        });

        // setup MQTT updates for publishing plug state and power updates
        MQTTUpdates mqttUpd = new MQTTUpdates(config.getMqttTopicPrefix());

        PlugSim.Observer observer = (name, key, value) -> { // Observer pattern for monitoring plug state and power changes
            try {
                mqtt.publish(mqttUpd.getTopic(name, key), mqttUpd.getMessage(value)); // Publish the updates plug state or power reading to the MQTT broker
            } catch (Exception e) {
                logger.error("fail to publish {} {} {}", name, key, value, e);
            }
        };

        for (PlugSim plug : plugs) { // Attach the observer to all plugs
            plug.addObserver(observer);

            // Publish initial state of each plug at startup
            observer.update(plug.getName(), "state", plug.isOn() ? "on" : "off");
            observer.update(plug.getName(), "power", String.format("%.3f", plug.getPower()));
        };
    }

    @Override
    public void close() throws Exception {
        http.close(); // cloase the HTTP server
        mqtt.disconnect(); // disconnect the MQTT client
    }
}