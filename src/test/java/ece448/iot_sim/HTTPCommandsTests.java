package ece448.iot_sim;

import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class HTTPCommandsTests {

    @Test
    public void testListPlugs() {
        PlugSim plug1 = new PlugSim("plug1");
        PlugSim plug2 = new PlugSim ("plug2");
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug1, plug2));

        String response = httpCommands.handleGet("/", new HashMap<>());

        assertTrue(response.contains("plug1"));
        assertTrue(response.contains("plug2"));
    }

    @Test
    public void testReportSinglePlug() {
        PlugSim plug = new PlugSim("testPlug");
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        String response = httpCommands.handleGet("/testPlug", new HashMap<>());
        assertTrue(response.contains("Plug testPlug is off"));
    }

    @Test
    public void testSwitchOnPlug() {
        PlugSim plug = new PlugSim("testPlug");
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>();
        params.put("action", "on");

        httpCommands.handleGet("/testPlug", params);
        assertTrue(plug.isOn());
    }

    @Test
    public void testSwitchOffPlug() {
        PlugSim plug = new PlugSim("testPlug");
        plug.switchOn();
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>();
        params.put("action", "off");

        httpCommands.handleGet("/testPlug", params);
        assertFalse(plug.isOn());
    }

}