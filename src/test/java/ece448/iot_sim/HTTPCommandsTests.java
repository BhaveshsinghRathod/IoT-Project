package ece448.iot_sim;

import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class HTTPCommandsTests {

    @Test // Test listing all available plugs
    public void testListPlugs() {
        PlugSim plug1 = new PlugSim("plug1");
        PlugSim plug2 = new PlugSim ("plug2"); // Creating 2 plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug1, plug2)); // Initializating HTTPCommands with the plugs

        String response = httpCommands.handleGet("/", new HashMap<>()); // Retrieving response for listening plugs

        assertTrue(response.contains("plug1"));
        assertTrue(response.contains("plug2"));// Verifying that both plugs are listed in the response
    }

    @Test // Test retrieving status of a single plug
    public void testReportSinglePlug() {
        PlugSim plug = new PlugSim("testPlug"); // Creating a plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        String response = httpCommands.handleGet("/testPlug", new HashMap<>()); // Retrieving report for the plug
        assertTrue(response.contains("Plug testPlug is off")); // Checking if the plug's sratus is correctly reported as off
    }

    @Test // Test switching a plug on
    public void testSwitchOnPlug() {
        PlugSim plug = new PlugSim("testPlug"); // Creating a plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>(); // Setting action to switch the plug on
        params.put("action", "on");

        httpCommands.handleGet("/testPlug", params); // Executing the action
        assertTrue(plug.isOn()); // Verifying that the plug is switched on
    }

    @Test // Test switching a plug off
    public void testSwitchOffPlug() {
        PlugSim plug = new PlugSim("testPlug"); // Creating a plug simulation
        plug.switchOn(); // switching on the plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>(); // Setting action to switch the plug off
        params.put("action", "off");

        httpCommands.handleGet("/testPlug", params); // Executing the action
        assertFalse(plug.isOn()); // Verifying that the plug is switched off
    }

    @Test // Test toggling a plug state
    public void testTogglePlug() {
        PlugSim plug = new PlugSim("testPlug"); // Creating a plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>(); // Setting action to toggle the plug
        params.put("action", "toggle");

        httpCommands.handleGet("/testPlug", params); // Executing the toggle action
        assertTrue(plug.isOn()); // Verifying that the plug is switched on
    }

    @Test // test toggling a plug twice returns to the original state
    public void testTogglePlugTwice() {
        PlugSim plug = new PlugSim("testPlug"); // Creating a plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>(); // Setting action to toggle the plug
        params.put("action", "toggle");

        httpCommands.handleGet("/testPlug", params);
        httpCommands.handleGet("/testPlug", params); // Toggling Twice
        assertFalse(plug.isOn()); // Verifying that the plug returns to its initial state i.e., off
    }

    @Test // Test reporting plug status after switching it on
    public void testReportAfterSwitchOn() {
        PlugSim plug = new PlugSim("testPlug"); // Creating a plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>(); // Switching the plug on
        params.put("action", "on");
        httpCommands.handleGet("/testPlug", params);

        String response = httpCommands.handleGet("/testPlug", new HashMap<>()); // Checking if the report correctly shows plug is on
        assertTrue(response.contains("Plug testPlug is on"));
    }

    @Test // Test reporting plug status after toggling it
    public void testReportAfterToggle() {
        PlugSim plug = new PlugSim("testPlug"); // Creating a plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>(); // Toggling the plug
        params.put("action", "toggle");
        httpCommands.handleGet("/testPlug", params);

        String response = httpCommands.handleGet("/testPlug", new HashMap<>()); // Checking if the report correctly shows plug as on
        assertTrue(response.contains("Plug testPlug is on"));
    }

    @Test // Test requesting a non-existing plug returns null
    public void testNonExistingPlug() {
        PlugSim plug = new PlugSim("plug1"); // Creating a known plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        String response = httpCommands.handleGet("/unknownPlug", new HashMap<>()); // Trying to access a non-existent plug
        assertNull(response); // Expecting null response since the plug doesn't exist
    }

    @Test // Test handling an invalid action without modifying the plug state
    public void testInvalidAction() {
        PlugSim plug = new PlugSim("testPlug"); // Creating a plug simulation
        HTTPCommands httpCommands = new HTTPCommands(Arrays.asList(plug));

        Map<String, String> params = new HashMap<>(); // Sending an invalid action
        params.put("action", "invalidAction");

        String response = httpCommands.handleGet("/testPlug", params); // Checking that response is not null i.e., indicating an error message is returned
        assertNotNull(response);
    }

}