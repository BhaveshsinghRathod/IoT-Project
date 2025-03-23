package ece448.iot_sim;

import static org.junit.Assert.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

public class MQTTIntegrationTest {
    private PlugSim plug1;
    private PlugSim plug2;
    private MQTTCommands mqttCommands;
    private MQTTUpdates mqttUpdates;
    private static final String PREFIX = "iot_ece448";

    @Before
    public void setUp() {
        plug1 = new PlugSim("plug1");
        plug2 = new PlugSim("plug2");
        List<PlugSim> plugs = Arrays.asList(plug1, plug2);
        mqttCommands = new MQTTCommands(plugs, PREFIX);
        mqttUpdates = new MQTTUpdates(PREFIX);
    }

    @Test
    public void testPlugToggle() {
        mqttCommands.handleMessage(PREFIX + "/action/plug1/toggle", new MqttMessage());
        assertTrue(plug1.isOn());
        mqttCommands.handleMessage(PREFIX + "/action/plug1/toggle", new MqttMessage());
        assertFalse(plug1.isOn());
    }

    @Test
    public void testPlugOnOff() {
        mqttCommands.handleMessage(PREFIX + "/action/plug2/on", new MqttMessage());
        assertTrue(plug2.isOn());
        mqttCommands.handleMessage(PREFIX + "/action/plug2/off", new MqttMessage());
        assertFalse(plug2.isOn());
    }

    @Test
    public void testInvalidAction() {
        plug1.switchOff();
        mqttCommands.handleMessage(PREFIX + "/action/plug1/invalid", new MqttMessage());
        assertFalse(plug1.isOn());
    }

    @Test
    public void testUnknownPlug() {
        mqttCommands.handleMessage(PREFIX + "/action/unknown/on", new MqttMessage());
        assertFalse(plug1.isOn());
        assertFalse(plug2.isOn());
    }

    @Test
    public void testStateUpdateTopic() {
        String topic = mqttUpdates.getTopic("plug1", "state");
        assertEquals(PREFIX + "/update/plug1/state", topic);
    }

    @Test
    public void testPowerUpdateTopic() {
        String topic = mqttUpdates.getTopic("plug2", "power");
        assertEquals(PREFIX + "/update/plug2/power", topic);
    }

    @Test
    public void testStateUpdateMessage() {
        MqttMessage message = mqttUpdates.getMessage("on");
        assertEquals("on", new String(message.getPayload()));
        assertTrue(message.isRetained());
    }

    @Test
    public void testPowerUpdateMessage() {
        MqttMessage message = mqttUpdates.getMessage("100.5");
        assertEquals("100.5", new String(message.getPayload()));
        assertTrue(message.isRetained());
    }

    @Test
    public void testCommandTopic() {
        String topic = mqttCommands.getTopic();
        assertEquals(PREFIX + "/action/#", topic);
    }
}
