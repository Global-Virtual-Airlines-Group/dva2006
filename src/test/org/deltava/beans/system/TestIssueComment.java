package org.deltava.beans.system;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestIssueComment extends AbstractBeanTestCase {
	
	private IssueComment _ic;
	
    public static Test suite() {
        return new CoverageDecorator(TestIssueComment.class, new Class[] { IssueComment.class } );
    }

	protected void setUp() throws Exception {
		super.setUp();
		_ic = new IssueComment(1, "Comments");
		setBean(_ic);
	}

	protected void tearDown() throws Exception {
		_ic = null;
		super.tearDown();
	}
	
	public void testProperties() {
		assertEquals(1, _ic.getID());
		assertEquals("Comments", _ic.getComments());
		checkProperty("createdBy", new Integer(45));
		checkProperty("issueID", new Integer(45));
		checkProperty("createdOn", new Date());
	}
	
	public void testValidation() {
		validateInput("ID", new Integer(2), IllegalStateException.class);
		try {
			_ic.setCreatedOn(null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException iae) {
			// empty
		}
	}

	public void testComparator() {
		_ic.setCreatedOn(new Date(System.currentTimeMillis() - 100));
		IssueComment ic2 = new IssueComment(2, "Comments");
		ic2.setCreatedOn(new Date());
		
		assertTrue(_ic.compareTo(ic2) < 0);
		assertTrue(ic2.compareTo(_ic) > 0);
	}
}