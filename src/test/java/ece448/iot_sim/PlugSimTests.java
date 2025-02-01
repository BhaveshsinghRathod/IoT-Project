package ece448.iot_sim;

import static org.junit.Assert.*;

import org.junit.Test;

public class PlugSimTests {

	@Test
	public void testInit() {
		PlugSim plug = new PlugSim("a");

		assertFalse(plug.isOn());
	}

	@Test
	public void testSwitchOn() {
		PlugSim plug = new PlugSim("a");

		plug.switchOn();

		assertTrue(plug.isOn());
	}
	
	@Test
	public void testSwitchOff() {
		PlugSim plug = new PlugSim("a");

		plug.switchOff();
		assertFalse(plug.isOn());
	}

	@Test
	public void testGetName() {
		PlugSim plug = new PlugSim("smartPlug123");

		assertEquals("smartPlug123", plug.getName());
	}
	@Test
	public void testToggle() {
		PlugSim plug = new PlugSim("a");

		plug.toggle();
		assertTrue(plug.isOn());

		plug.toggle();
		assertFalse(plug.isOn());
	}

	@Test
	public void testMeasurePowerWhenOff() {
		PlugSim plug = new PlugSim("a");

		plug.measurePower();
		assertEquals(0.0, plug.getPower(), 0.001);
	}

	@Test
	public void testPowerUpdateBasedOnName() {
		PlugSim plug = new PlugSim("test.200");
		plug.switchOn();
		plug.measurePower();

		assertEquals(200.0, plug.getPower(), 0.001);

	}

	@Test
	public void testPowerFLuctuationRange() {
		PlugSim plug = new PlugSim("a");
		plug.switchOn();
		for (int i = 0; i < 10; i++) {
			plug.measurePower();
		}

		double power = plug.getPower();
		assertTrue(power >= 0 && power <=300);
	}

	@Test
	public void testPowerRemainsZeroWhenOff() {
		PlugSim plug = new PlugSim("a");

		for (int i = 0; i < 5; i++) {
			plug.measurePower();
		}

		assertEquals(0.0, plug.getPower(), 0.001);
	}


	@Test
	public void testPowerDecreasesWhenAbove300() {
		PlugSim plug = new PlugSim("plug");


		plug.switchOn();
		plug.updatePower(350);

		for (int i = 0; i < 10; i++) {
			plug.measurePower();

			if (plug.getPower() <= 300) {
				break;
			}
		}

		assertTrue("Power should be less than 300", plug.getPower() <= 300);
	}

	@Test
	public void testInvalidPowerValueInName() {
		PlugSim plug = new PlugSim("plug.invalid");

		plug.switchOn();
		plug.measurePower();
		assertTrue(plug.getPower() >= 0);
	}

}
