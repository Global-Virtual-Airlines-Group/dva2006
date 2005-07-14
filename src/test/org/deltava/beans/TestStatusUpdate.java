package org.deltava.beans;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.StatusUpdate;

public class TestStatusUpdate extends AbstractBeanTestCase {

	private StatusUpdate _upd;
	
	public static Test suite() {
        return new CoverageDecorator(TestStatusUpdate.class, new Class[] { StatusUpdate.class } );
    }
	
	protected void setUp() throws Exception {
		super.setUp();
		_upd = new StatusUpdate(1, StatusUpdate.INTPROMOTION);
		setBean(_upd);
	}

	protected void tearDown() throws Exception {
		_upd = null;
		super.tearDown();
	}
	
	public void testProperties() {
		assertEquals(1, _upd.getID());
		assertEquals(StatusUpdate.INTPROMOTION, _upd.getType());
		assertEquals(StatusUpdate.TYPES[StatusUpdate.INTPROMOTION], _upd.getTypeName());
		checkProperty("firstName", "John");
		checkProperty("lastName", "Smith");
		checkProperty("authorID", new Integer(1234));
		checkProperty("description", "This is a test");
		checkProperty("createdOn", new Date());
	}
	
	public void testValidation() {
		validateInput("ID", new Integer(0), IllegalArgumentException.class);
		validateInput("type", new Integer(-1), IllegalArgumentException.class);
		validateInput("type", new Integer(21), IllegalArgumentException.class);
	}
	
	public void testComparator() {
		long now = System.currentTimeMillis();
		_upd.setCreatedOn(new Date(now));
		
		StatusUpdate upd2 = new StatusUpdate(2, 1);
		upd2.setCreatedOn(new Date(now + 15));
		
		assertTrue(_upd.compareTo(upd2) < 0);
		assertTrue(upd2.compareTo(_upd) > 0);
	}
}