package org.deltava.beans.system;

import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestIssue extends AbstractBeanTestCase {

	private Issue _i;
	
    public static Test suite() {
        return new CoverageDecorator(TestIssue.class, new Class[] { Issue.class } );
    }
	
	protected void setUp() throws Exception {
		super.setUp();
		_i = new Issue(1, "Subject");
		setBean(_i);
	}

	protected void tearDown() throws Exception {
		_i = null;
		super.tearDown();
	}

	public void testProperties() {
		assertEquals(1, _i.getID());
		assertEquals("Subject", _i.getSubject());
		checkProperty("description", "blah blah blah");
		checkProperty("type", new Integer(1));
		checkProperty("area", new Integer(2));
		checkProperty("status", new Integer(1));
		checkProperty("priority", new Integer(3));
		checkProperty("createdBy", new Integer(123));
		checkProperty("assignedTo", new Integer(124));
		checkProperty("majorVersion", new Integer(3));
		checkProperty("minorVersion", new Integer(1));
		checkProperty("createdOn", new Date());
		checkProperty("resolvedOn", new Date());
		checkProperty("lastCommentOn", new Date());
		checkProperty("commentCount", new Integer(23));
		_i.setResolvedOn(null);
		_i.setLastCommentOn(null);
		
		assertEquals(Issue.PRIORITY[_i.getPriority()], _i.getPriorityName());
		assertEquals(Issue.STATUS[_i.getStatus()], _i.getStatusName());
		assertEquals(Issue.TYPE[_i.getType()], _i.getTypeName());
		assertEquals(Issue.AREA[_i.getArea()], _i.getAreaName());
	}
	
	public void testValidation() {
		validateInput("type", new Integer(-1), IllegalArgumentException.class);
		validateInput("type", new Integer(6), IllegalArgumentException.class);
		validateInput("area", new Integer(-1), IllegalArgumentException.class);
		validateInput("area", new Integer(6), IllegalArgumentException.class);
		validateInput("status", new Integer(-1), IllegalArgumentException.class);
		validateInput("status", new Integer(6), IllegalArgumentException.class);
		validateInput("priority", new Integer(-1), IllegalArgumentException.class);
		validateInput("priority", new Integer(6), IllegalArgumentException.class);
		validateInput("commentCount", new Integer(-1), IllegalArgumentException.class);
		
		validateInput("majorVersion", new Integer(-1), IllegalArgumentException.class);
		validateInput("minorVersion", new Integer(-1), IllegalArgumentException.class);
		
		validateInput("type", "XXXX", IllegalArgumentException.class);
		validateInput("area", "XXXX", IllegalArgumentException.class);
		validateInput("status", "XXXX", IllegalArgumentException.class);
		validateInput("priority", "XXXX", IllegalArgumentException.class);
		
		validateInput("type", null, IllegalArgumentException.class);
		validateInput("area", null, IllegalArgumentException.class);
		validateInput("status", null, IllegalArgumentException.class);
		validateInput("priority", null, IllegalArgumentException.class);
		
		_i.addComment(new IssueComment(1, "Comment"));
		validateInput("commentCount", new Integer(2), IllegalStateException.class);
		
		try {
			_i.setCreatedOn(null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException iae) { 
			// empty
		}
		
		_i.setCreatedOn(new Date());
		validateInput("resolvedOn", new Date(System.currentTimeMillis() - 100), IllegalArgumentException.class);
		validateInput("lastCommentOn", new Date(System.currentTimeMillis() - 100), IllegalArgumentException.class);
	}
	
	public void testComments() {
		assertNotNull(_i.getComments());
		assertEquals(0, _i.getComments().size());
		
		IssueComment ic = new IssueComment(1, "Comment 1");
		IssueComment ic2 = new IssueComment(2, "Comment 2");
		ic2.setCreatedOn(new Date(System.currentTimeMillis() - 100));
		
		_i.addComment(ic);
		assertEquals(1, _i.getComments().size());
		assertEquals(_i.getComments().size(), _i.getCommentCount());
		assertEquals(ic, new ArrayList<IssueComment>(_i.getComments()).get(0));
		
		_i.addComment(ic2);
		assertEquals(2, _i.getComments().size());
		assertEquals(ic2, new ArrayList<IssueComment>(_i.getComments()).get(0));
		assertEquals(_i.getComments().size(), _i.getCommentCount());
	}
	
	public void testComparator() {
		Issue i2 = new Issue(2, "Subject 2");
		
		assertTrue(_i.compareTo(i2) < 0);
		assertTrue(i2.compareTo(_i) > 0);
	}
	
	public void testViewEntry() {
	   String[] CLASSES = {"opt1", null, "warn", "err"};
	   for (int x = 0; x < CLASSES.length; x++) {
	      _i.setStatus(x);
	      assertEquals(CLASSES[x], _i.getRowClassName());
	   }
	}
}