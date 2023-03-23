package org.deltava.dao.file;

import java.io.*;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import org.deltava.beans.acars.RouteEntry;
import org.deltava.beans.acars.SerializedDataVersion;

import junit.framework.TestCase;

public class TestSerializedPositions extends TestCase {

	private static byte[] load(File f) throws IOException {
		try (InputStream bis = new BufferedInputStream(new FileInputStream(f)); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			int b = bis.read();
			while (b != -1) {
				bos.write(b);
				b = bis.read();
			}
					
			return bos.toByteArray();
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV92() throws Exception {
		
		File f = new File("data/acars/ACARSv92.dat");
		assertTrue(f.exists());

		byte[] data = load(f); 
		assertNotNull(data);
		assertEquals(f.length(), data.length);
		
		int fw = ((data[1] << 8) & 0xFF00) + data[0];
		assertTrue(fw == GZIPInputStream.GZIP_MAGIC);
		
		try (InputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv92, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV91() throws Exception {
		
		File f = new File("data/acars/ACARSv91.dat");
		assertTrue(f.exists());
		
		byte[] data = load(f); 
		assertNotNull(data);
		assertEquals(f.length(), data.length);
		
		int fw = ((data[1] << 8) & 0xFF00) + data[0];
		assertTrue(fw == GZIPInputStream.GZIP_MAGIC);
		
		try (InputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv91, posdao.getFormat());
		}
	}
}