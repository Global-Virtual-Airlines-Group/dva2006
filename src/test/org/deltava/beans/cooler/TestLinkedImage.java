package org.deltava.beans.cooler;

import java.util.*;

import junit.framework.Test;

import org.deltava.beans.*;

import org.hansel.CoverageDecorator;

public class TestLinkedImage extends AbstractBeanTestCase {

	private LinkedImage _img;
	
    public static Test suite() {
        return new CoverageDecorator(TestLinkedImage.class, new Class[] { LinkedImage.class } );
    }
	
	protected void setUp() throws Exception {
		super.setUp();
		_img = new LinkedImage(1, "http://localhost/test");
		setBean(_img);
	}

	protected void tearDown() throws Exception {
		_img = null;
		super.tearDown();
	}

	public void testProperties() {
		assertEquals(1, _img.getID());
		assertEquals("http://localhost/test", _img.getURL());
		checkProperty("description", "test");
		checkProperty("URL", "http://localhost/dev/null");
		assertEquals(_img.getURL().hashCode(), _img.hashCode());
		assertEquals(_img.getURL(), _img.toString());
	}
	
	public void testValidation() {
		validateInput("ID", new Integer(-1), IllegalArgumentException.class);
		validateInput("URL", "crap", IllegalArgumentException.class);
	}
	
	public void testEquality() {
		assertFalse(_img.equals(null));
		assertFalse(_img.equals(new Object()));
	}
	
	public void testHashCode() {
		Collection<LinkedImage> imgs = new HashSet<LinkedImage>();
		imgs.add(_img);
		assertEquals(1, imgs.size());
		assertTrue(imgs.contains(_img));
		LinkedImage img2 = new LinkedImage(_img.getID() + 1, _img.getURL());
		assertNotNull(img2);
		assertEquals(_img.hashCode(), img2.hashCode());
		assertTrue(_img.equals(img2));
		imgs.add(img2);
		assertEquals(1, imgs.size());
		assertTrue(imgs.contains(_img));
	}
}