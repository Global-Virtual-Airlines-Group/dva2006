package org.deltava.beans.system;

import java.time.Instant;
import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestIssue extends AbstractBeanTestCase {

	private Issue _i;
	
    public static Test suite() {
        return new CoverageDecorator(TestIssue.class, new Class[] { Issue.class } );
    }
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_i = new Issue(1, "Subject");
		setBean(_i);
	}

	@Override
	protected void tearDown() throws Exception {
		_i = null;
		super.tearDown();
	}

	public void testProperties() {
		assertEquals(1, _i.getID());
		assertEquals("Subject", _i.getSubject());
		checkProperty("description", "blah blah blah");
		checkProperty("type", Integer.valueOf(1));
		checkProperty("area", Integer.valueOf(2));
		checkProperty("status", Integer.valueOf(1));
		checkProperty("priority", Integer.valueOf(3));
		checkProperty("createdBy", Integer.valueOf(123));
		checkProperty("assignedTo", Integer.valueOf(124));
		checkProperty("majorVersion", Integer.valueOf(3));
		checkProperty("minorVersion", Integer.valueOf(1));
		checkProperty("createdOn", Instant.now());
		checkProperty("resolvedOn", Instant.now());
		checkProperty("lastCommentOn", Instant.now());
		checkProperty("commentCount", Integer.valueOf(23));
		_i.setResolvedOn(null);
		_i.setLastCommentOn(null);
	}
	
	public void testValidation() {
		validateInput("type", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("type", Integer.valueOf(6), IllegalArgumentException.class);
		validateInput("area", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("area", Integer.valueOf(6), IllegalArgumentException.class);
		validateInput("status", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("status", Integer.valueOf(6), IllegalArgumentException.class);
		validateInput("priority", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("priority", Integer.valueOf(6), IllegalArgumentException.class);
		validateInput("commentCount", Integer.valueOf(-1), IllegalArgumentException.class);
		
		validateInput("majorVersion", Integer.valueOf(-1), IllegalArgumentException.class);
		validateInput("minorVersion", Integer.valueOf(-1), IllegalArgumentException.class);
		
		validateInput("type", "XXXX", IllegalArgumentException.class);
		validateInput("area", "XXXX", IllegalArgumentException.class);
		validateInput("status", "XXXX", IllegalArgumentException.class);
		validateInput("priority", "XXXX", IllegalArgumentException.class);
		
		validateInput("type", null, IllegalArgumentException.class);
		validateInput("area", null, IllegalArgumentException.class);
		validateInput("status", null, IllegalArgumentException.class);
		validateInput("priority", null, IllegalArgumentException.class);
		
		_i.add(new IssueComment(1, "Comment"));
		validateInput("commentCount", Integer.valueOf(2), IllegalStateException.class);
		
		try {
			_i.setCreatedOn(null);
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException iae) { 
			// empty
		}
		
		_i.setCreatedOn(Instant.now());
		validateInput("resolvedOn", new Date(System.currentTimeMillis() - 100), IllegalArgumentException.class);
		validateInput("lastCommentOn", new Date(System.currentTimeMillis() - 100), IllegalArgumentException.class);
	}
	
	public void testComments() {
		assertNotNull(_i.getComments());
		assertEquals(0, _i.getComments().size());
		
		IssueComment ic = new IssueComment(1, "Comment 1");
		IssueComment ic2 = new IssueComment(2, "Comment 2");
		ic2.setCreatedOn(Instant.now().minusMillis(100));
		
		_i.add(ic);
		assertEquals(1, _i.getComments().size());
		assertEquals(_i.getComments().size(), _i.getCommentCount());
		assertEquals(ic, new ArrayList<IssueComment>(_i.getComments()).get(0));
		
		_i.add(ic2);
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
	      _i.setStatus(IssueStatus.values()[x]);
	      assertEquals(CLASSES[x], _i.getRowClassName());
	   }
	}
}