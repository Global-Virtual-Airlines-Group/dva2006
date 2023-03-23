package org.deltava.beans;

import java.io.*;

import junit.framework.TestCase;

public class TestCompression extends TestCase {
	
	@SuppressWarnings("static-method")
	public void testFileName() {
		assertEquals(Compression.BZIP2, Compression.get("foo.bz2"));
		assertEquals(Compression.GZIP, Compression.get("foo.gz"));
		assertEquals(Compression.BROTLI, Compression.get("foo.br"));
		assertEquals(Compression.NONE, Compression.get("foo.txt"));
		assertEquals(Compression.NONE, Compression.get("foo"));
	}

	@SuppressWarnings("static-method")
	public void testDetectFile() throws IOException {

		File f = new File("data/file/filedata.txt");
		assertTrue(f.exists());
		assertEquals(Compression.NONE, Compression.detect(f));
		
		f = new File("data/file/filedata.txt.gz");
		assertTrue(f.exists());
		assertEquals(Compression.GZIP, Compression.detect(f));
		
		f = new File("data/file/filedata.txt.bz2");
		assertTrue(f.exists());
		assertEquals(Compression.BZIP2, Compression.detect(f));
	}
}