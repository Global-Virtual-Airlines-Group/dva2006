package org.deltava.beans;

import java.io.*;
import java.util.zip.*;

import junit.framework.*;

import org.hansel.CoverageDecorator;

public class TestFileUpload extends TestCase {
	
	private FileUpload _fu;
	
	public static Test suite() {
		return new CoverageDecorator(TestFileUpload.class, new Class[] { FileUpload.class } );
   }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_fu = new FileUpload("file.name");
	}

	@Override
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
        File f = new File("data/testImage.GIF");
        assertTrue(f.exists());
        try (InputStream is = new FileInputStream(f)) { 
        	_fu.load(is);
        }
        		
        assertNotNull(_fu.getBuffer());
        assertEquals(f.length(), _fu.getSize());
	}
	
	@SuppressWarnings("static-method")
	public void testGZStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (GZIPOutputStream gzout = new GZIPOutputStream(out)) {
			try (PrintWriter pw = new PrintWriter(gzout)) {
				pw.println("Line 1");
				pw.println("Line 2");
			}
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		try (GZIPInputStream gzin = new GZIPInputStream(in)) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(gzin))) {
				assertEquals("Line 1", br.readLine());
				assertEquals("Line 2", br.readLine());
				assertFalse(br.ready());
			}
		}
	}
}