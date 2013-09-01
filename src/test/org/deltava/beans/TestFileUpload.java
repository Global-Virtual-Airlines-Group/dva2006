package org.deltava.beans;

import java.io.*;
import java.util.zip.*;

import junit.framework.*;

import org.hansel.CoverageDecorator;
import org.apache.commons.compress.compressors.bzip2.*;

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
	
	public void testBZ2Stream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (BZip2CompressorOutputStream bzout = new BZip2CompressorOutputStream(out, 4)) {
			try (PrintWriter pw = new PrintWriter(bzout)) {
				pw.println("Line 1");
				pw.println("Line 2");
			}
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		try (BZip2CompressorInputStream bzin = new BZip2CompressorInputStream(in)) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(bzin))) {
				assertEquals("Line 1", br.readLine());
				assertEquals("Line 2", br.readLine());
				assertFalse(br.ready());
			}
		}
	}
	
	public void testBZ2File() throws IOException {
		
		File f = new File("data/test.txt.bz2");
		assertTrue(f.exists());
		try (BZip2CompressorInputStream bzin = new BZip2CompressorInputStream(new BufferedInputStream(new FileInputStream(f)), true)) {
			try (LineNumberReader br = new LineNumberReader(new InputStreamReader(bzin))) {
				assertEquals("Line 1", br.readLine());
				assertEquals("Line 2", br.readLine());
			}
		}
		
		FileUpload fu = new FileUpload("test.txt.bz2");
		fu.load(new FileInputStream(f));
		
		try (InputStream is = fu.getInputStream()) {
			assertTrue(is instanceof BZip2CompressorInputStream);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				assertEquals("Line 1", br.readLine());
				assertEquals("Line 2", br.readLine());
			}
		}
	}
}