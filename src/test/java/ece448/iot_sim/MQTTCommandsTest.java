package ece448.iot_sim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class MQTTCommandsTest {

    private MQTTCommands mqttCommands; // Instance of MQTTCommands to be tested
    private List<PlugSim> plugs; // List of plug sim for testing
    private static final String PREFIX = "iot_ece448"; // MQTT topic prefix used for communication

    @Before
    public void setUp() { // Initialize the list of simulated plugs
        plugs = new ArrayList<>();
        plugs.add(new PlugSim("plug1"));
        plugs.add(new PlugSim("plug2"));
        mqttCommands = new MQTTCommands(plugs, PREFIX); // Initialize the MQTTCommands instance with the simulated plugs and topic prefix
    }

    @Test // Verify that the topic subscription format is correct
    public void testGetTopic() {
        assertEquals(PREFIX + "/action/#", mqttCommands.getTopic());
    }

    @Test // Verify that the topic generated for plug updates follows the expected format
    public void testMQTTUpdatesGetTopic() {
        MQTTUpdates mqttUpdates = new MQTTUpdates(PREFIX);
        String expectedTopic = PREFIX + "/update/plug1/state";
        assertEquals(expectedTopic, mqttUpdates.getTopic("plug1", "state"));
    }

    @Test // Test that the MQTT message payload matched the expected value ("on")
    public void testMQTTUpdatesGetMessage() {
        MQTTUpdates mqttUpdates = new MQTTUpdates(PREFIX);
        String value = "on";
        MqttMessage message = mqttUpdates.getMessage(value);
        assertArrayEquals(value.getBytes(), message.getPayload()); // Verify that the message payload matched the expected byte array
        assertTrue(message.isRetained()); // Ensure that the message is marked as retained
    }

    @Test // Test that the MQTT message payload matched the expected value 
    public void testMQTTUpdatesGetMessageDifferentValue() {
        MQTTUpdates mqttUpdates = new MQTTUpdates(PREFIX);
        String value = "off";
        MqttMessage message = mqttUpdates.getMessage(value);
        assertArrayEquals(value.getBytes(), message.getPayload()); // Verify that the message payload matched the expected byte array
        assertTrue(message.isRetained()); // Ensure that the message is marked as retined
    }

    @Test // Tests to check how the system handles invalid MQTT messages
    public void testHandleMessageInvalidTopic() {
        String topic = "invalid/topic"; // Simulate receiving an MQTT message with an invalid topic
        MqttMessage message = new MqttMessage();
        mqttCommands.handleMessage(topic, message); // Pass the invalid message to the handler
    }

    @Test // Simulate receiving an MQTT message for a plug that does not exist
    public void testHandleMessageNonexistentPlug() {
        String topic = PREFIX + "/action/nonexistent/toggle";
        MqttMessage message = new MqttMessage();
        mqttCommands.handleMessage(topic, message); // Pass the message to the handler
    }
}
