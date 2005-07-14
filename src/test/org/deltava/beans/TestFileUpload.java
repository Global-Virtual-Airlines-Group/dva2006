// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestCase;

import org.hansel.CoverageDecorator;

public class TestFileUpload extends TestCase {
	
	private FileUpload _fu;
	
	public static Test suite() {
		return new CoverageDecorator(TestFileUpload.class, new Class[] { FileUpload.class } );
   }

	protected void setUp() throws Exception {
		super.setUp();
		_fu = new FileUpload("file.name");
	}

	protected void tearDown() throws Exception {
		_fu = null;
		super.tearDown();
	}
	
	public void testProperties() {
		assertEquals("file.name", _fu.getName());
		assertEquals(0, _fu.getSize());
		assertNull(_fu.getBuffer());
	}
	
	public void testLoad() throws IOException {
        File f = new File("data/testImage.gif");
        assertTrue(f.exists());
        InputStream is = new FileInputStream(f);
        _fu.load(is);
        assertNotNull(_fu.getBuffer());
        assertEquals(f.length(), _fu.getSize());
	}
}