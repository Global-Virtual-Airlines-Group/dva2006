package org.deltava.beans.schedule;

import java.util.Comparator;

import junit.framework.TestCase;

public class TestScheduleSource extends TestCase {

	@SuppressWarnings("static-method")
	public void testComparator() {
		
		Comparator<ScheduleSource> cmp = ScheduleSource.comparator();
		assertNotNull(cmp);
		
		assertTrue(cmp.compare(ScheduleSource.VASYS, ScheduleSource.DELTA) < 0);
		assertTrue(cmp.compare(ScheduleSource.DELTA, ScheduleSource.INNOVATA) < 0);
		
		assertTrue(ScheduleSource.AVSTACK.isPrimary());
		assertEquals(1, cmp.compare(ScheduleSource.VASYS, ScheduleSource.AVSTACK));
	}
}