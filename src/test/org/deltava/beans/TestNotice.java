package org.deltava.beans;

import java.time.Instant;
import java.util.Date;

import junit.framework.Test;

import org.hansel.CoverageDecorator;

public class TestNotice extends AbstractBeanTestCase {

	private Notice _n;

   public static Test suite() {
       return new CoverageDecorator(TestNotice.class, new Class[] { Notice.class, News.class } );
   }
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_n = new Notice("SUBJ", "AUTHOR", "BODY");
		setBean(_n);
	}

	@Override
	protected void tearDown() throws Exception {
		_n = null;
		super.tearDown();
	}
	
	public void testConstructor() {
		assertEquals("SUBJ", _n.getSubject());
		assertEquals("AUTHOR", _n.getAuthorName());
		assertEquals("BODY", _n.getBody());
		assertEquals(0, _n.getAuthorID());
		assertNotNull(_n.getDate());
		assertTrue(_n.getActive());
	}
	
	public void testNoticeInEffect() {
	   _n.setActive(false);
	   assertEquals("opt1", _n.getRowClassName());
	   checkProperty("active", Boolean.valueOf(true));
	   assertTrue(_n.getActive());
	   assertNull(_n.getRowClassName());
	}
	
	public void testProperties() {
	    checkProperty("authorID", Integer.valueOf(123));
	    checkProperty("date", new Date());
	}
	
	public void testValidation() {
	    validateInput("authorID", Integer.valueOf(0), IllegalArgumentException.class);
	}
	
	public void testComparator() {
	    News n2 = new News("SUBJ2", "AUTHOR", "BODY2");
	    n2.setDate(Instant.now().plusSeconds(2));
	    assertTrue(_n.compareTo(n2) > 0);
	    assertTrue(n2.compareTo(_n) < 0);
	}
}