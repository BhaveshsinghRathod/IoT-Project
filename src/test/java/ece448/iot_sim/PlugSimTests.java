package ece448.iot_sim;

import static org.junit.Assert.*;

import org.junit.Test;

public class PlugSimTests {

	@Test
	public void testInit() {
		PlugSim plug = new PlugSim("a");

		assertFalse(plug.isOn());
	}

	@Test // Call "switchOn()" to turn the plug "on"
	public void testSwitchOn() {
		PlugSim plug = new PlugSim("a");

		plug.switchOn();

		assertTrue(plug.isOn()); // the plug will be "on"
	}
	
	@Test // Call "switchOff()" to turn the plug "off"
	public void testSwitchOff() {
		PlugSim plug = new PlugSim("a");

		plug.switchOff();
		assertFalse(plug.isOn()); // the plug will be "off"
	}

	@Test // Call "GetName" to test if the name of the plug is correct
	public void testGetName() {
		PlugSim plug = new PlugSim("smartPlug123");

		assertEquals("smartPlug123", plug.getName()); // Verification of the actual name with the expected name
	}

	@Test // Call "Toggle" to switch the state of "plug"
	public void testToggle() {
		PlugSim plug = new PlugSim("a");

		plug.toggle();
		assertTrue(plug.isOn()); // 1st toggle turns the switch "on"

		plug.toggle();
		assertFalse(plug.isOn()); // 2nd toggle turns the switch "off"
	}

	@Test // Call "MeasurePowerWhenOff" to confirm if the power remains "zero" when plug is "off"
	public void testMeasurePowerWhenOff() {
		PlugSim plug = new PlugSim("a");

		plug.measurePower();
		assertEquals(0.0, plug.getPower(), 0.001);
	}

	@Test // Call "PowerUpdateBasedOnName" to set the power based on the numerical value in the name
	public void testPowerUpdateBasedOnName() {
		PlugSim plug = new PlugSim("test.200");
		plug.switchOn();
		plug.measurePower();

		assertEquals(200.0, plug.getPower(), 0.001); // since the numerical value in the name is "200", the power will be set to "200"

	}

	@Test // Call "PowerFLuctuationRange" to check if the power remains within the range even after multiple measures
	public void testPowerFLuctuationRange() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		for (int i = 0; i < 10; i++) {
			plug.measurePower();
		}

		double power = plug.getPower();
		assertTrue(power >= 0 && power <=300); // the power will remain in the range of "0 to 300"
	}

	@Test // Call "PowerRemainsZeroWhenOff" to ensure that power if "zero" when the plug is "off"
	public void testPowerRemainsZeroWhenOff() {
		PlugSim plug = new PlugSim("a");

		for (int i = 0; i < 5; i++) {
			plug.measurePower();
		}

		assertEquals(0.0, plug.getPower(), 0.001);
	}


	@Test // Call "PowerDecreasesWhenAbove300" to reduce the power whenever it goes beyond "300"
	public void testPowerDecreasesWhenAbove300() {
		PlugSim plug = new PlugSim("plug");


		plug.switchOn();
		plug.updatePower(350); // power is set to "350" i.e. more than "300"

		for (int i = 0; i < 10; i++) {
			plug.measurePower();

			if (plug.getPower() <= 300) {
				break; // breaks, since the power goes beyond "300"
			}
		}

		assertTrue("Power should be less than 300", plug.getPower() <= 300);
	}

	@Test // Call "InvalidPowerValueInName" so that invalid value of power in name does not cause error
	public void testInvalidPowerValueInName() {
		PlugSim plug = new PlugSim("plug.invalid");

		plug.switchOn();
		plug.measurePower();
		assertTrue(plug.getPower() >= 0); // ensures power remain valid
	}

}
