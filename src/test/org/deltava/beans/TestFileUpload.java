package org.deltava.beans;

import java.io.*;
import java.util.zip.*;

import junit.framework.*;

import org.hansel.CoverageDecorator;

import org.apache.commons.compress.compressors.bzip2.*;

import org.deltava.util.BZip2MultiInputStream;

public class TestFileUpload extends TestCase {
	
	private FileUpload _fu;

	private static final int MAX_LINES = 2500;
	
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
		try (GZIPOutputStream gzout = new GZIPOutputStream(out); PrintWriter pw = new PrintWriter(gzout)) {
			for (int x = 0; x < MAX_LINES; x++)
				pw.println("Line " + (x+1));	
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		try (GZIPInputStream gzin = new GZIPInputStream(in); BufferedReader br = new BufferedReader(new InputStreamReader(gzin))) {
			for (int x = 0; x < MAX_LINES; x++)
				assertEquals("Line " + (x+1), br.readLine());
			
			assertFalse(br.ready());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testBZStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (BZip2CompressorOutputStream bzout = new BZip2CompressorOutputStream(out); PrintWriter pw = new PrintWriter(bzout)) {
			for (int x = 0; x < MAX_LINES; x++)
				pw.println("Line " + (x+1));
		}

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		try (BZip2MultiInputStream bzin = new BZip2MultiInputStream(in); BufferedReader br = new BufferedReader(new InputStreamReader(bzin))) {
			for (int x = 0; x < MAX_LINES; x++)
				assertEquals("Line " + (x+1), br.readLine());
			
			assertFalse(br.ready());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testBZ2MultiStream() throws IOException {
		File f = new File("data/pssawy.dat.bz2");
		assertTrue(f.exists());
		
		int lineCount = 0;
		try (BZip2MultiInputStream bzin = new BZip2MultiInputStream(new FileInputStream(f)); BufferedReader br = new BufferedReader(new InputStreamReader(bzin), 131072)) {
			String data = br.readLine();
			while (data != null) {
				lineCount++;
				data = br.readLine();
			}
		}
		
		// Multi-stream; regular BZ2 stream will fail
		assertTrue(lineCount > 0);
		try (BZip2CompressorInputStream bzin = new BZip2CompressorInputStream(new FileInputStream(f)); BufferedReader br = new BufferedReader(new InputStreamReader(bzin), 131072)) {
			int lineCount2 = 0; String data = br.readLine();
			while (data != null) {
				lineCount2++;
				data = br.readLine();
			}
				
			assertTrue(lineCount > lineCount2);
		}
	}
}