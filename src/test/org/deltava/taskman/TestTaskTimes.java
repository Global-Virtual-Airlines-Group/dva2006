package org.deltava.taskman;

import java.time.*;
import java.time.temporal.ChronoUnit;

import junit.framework.*;

import org.apache.log4j.PropertyConfigurator;

public class TestTaskTimes extends TestCase {

	private Task _t;

	private class MockTask extends Task {
		MockTask(String name) {
			super(name, MockTask.class);
		}

		@Override
		protected void execute(TaskContext ctx) {
			// noop
		}
	}

	private static Instant time(int h, int m) {
		return ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).plusHours(h).plusMinutes(m).toInstant();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		_t = new MockTask("Test Task");
		_t.setEnabled(true);
	}

	@Override
	protected void tearDown() throws Exception {
		_t = null;
		super.tearDown();
	}

	public void testTimes() {
		_t.setRunTimes("hour", "14");
		_t.setRunTimes("min", "0");
		assertTrue(_t.isRunnable(time(14, 0)));
		assertFalse(_t.isRunnable(time(14, 1)));

		_t.setRunTimes("min", "*");
		assertTrue(_t.isRunnable(time(14, 0)));
		assertTrue(_t.isRunnable(time(14, 1)));

		_t.setRunTimes("hour", "*");
		_t.setRunTimes("min", "0");
		assertTrue(_t.isRunnable(time(14, 0)));
		assertFalse(_t.isRunnable(time(14, 1)));
		assertTrue(_t.isRunnable(time(4, 0)));
		assertFalse(_t.isRunnable(time(4, 1)));

		_t.setEnabled(false);
		assertFalse(_t.isRunnable(time(14, 0)));
		assertFalse(_t.isRunnable(time(14, 1)));
	}
}