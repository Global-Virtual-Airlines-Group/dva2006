package org.deltava.beans.fleet;

import junit.framework.Test;

import org.deltava.beans.AbstractBeanTestCase;
import org.hansel.CoverageDecorator;

public class TestResource extends AbstractBeanTestCase {
	
	private Resource _r;
	
    public static Test suite() {
        return new CoverageDecorator(TestResource.class, new Class[] { Resource.class });
    }

	protected void setUp() throws Exception {
		super.setUp();
		_r = new Resource("http://localhost/test");
		assertNotNull(_r);
		setBean(_r);
	}

	protected void tearDown() throws Exception {
		_r = null;
		super.tearDown();
	}

	public void testProperties() {
		assertEquals("http://localhost/test", _r.getURL());
		checkProperty("description", "Description Text");
		checkProperty("authorID", new Integer(1234));
		checkProperty("lastUpdateID", new Integer(234));
		checkProperty("hits", new Integer(3456));
		checkProperty("createdOn", new java.util.Date());
		checkProperty("public", Boolean.TRUE);
		assertEquals(_r.getURL(), _r.toString());
	}
	
	public void testValidation() {
		validateInput("authorID", new Integer(-1), IllegalArgumentException.class);
		validateInput("authorID", new Integer(0), IllegalArgumentException.class);
		validateInput("hits", new Integer(-1), IllegalArgumentException.class);
		validateInput("lastUpdateID", new Integer(-1), IllegalArgumentException.class);
		validateInput("lastUpdateID", new Integer(0), IllegalArgumentException.class);
		validateInput("URL", "CRAP", IllegalArgumentException.class);
	}
}