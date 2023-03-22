package org.deltava.dao.file;

import java.io.*;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import org.deltava.beans.acars.RouteEntry;

import junit.framework.TestCase;

public class TestSerializedPositions extends TestCase {

	@SuppressWarnings("static-method")
	public void testLoadPositions() throws Exception {
		
		File f = new File("data/acars/1a8419.dat");
		assertTrue(f.exists());

		byte[] data = null; 
		try (InputStream bis = new BufferedInputStream(new FileInputStream(f)); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			int b = bis.read();
			while (b != -1) {
				bos.write(b);
				b = bis.read();
			}
					
			data = bos.toByteArray();
		}
		
		assertNotNull(data);
		assertTrue(data.length > 10);
		
		int fw = ((data[1] << 8) & 0xFF00) + data[0];
		assertTrue(fw == GZIPInputStream.GZIP_MAGIC);
		
		try (InputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
		}
	}
}