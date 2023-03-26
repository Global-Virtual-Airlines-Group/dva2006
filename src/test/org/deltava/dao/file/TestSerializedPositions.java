package org.deltava.dao.file;

import java.io.*;
import java.util.Collection;

import org.deltava.beans.Compression;
import org.deltava.beans.acars.RouteEntry;
import org.deltava.beans.acars.SerializedDataVersion;

import junit.framework.TestCase;

public class TestSerializedPositions extends TestCase {

	@SuppressWarnings("static-method")
	public void testV0() throws Exception {
		
		File f = new File("data/acars/ACARSv0.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARS, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testXACARS() throws Exception {
		
		File f = new File("data/acars/XACARS.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.XACARS, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV2() throws Exception {
		
		File f = new File("data/acars/ACARSv2.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv2, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV3() throws Exception {
		
		File f = new File("data/acars/ACARSv3.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv3, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV4() throws Exception {
		
		File f = new File("data/acars/ACARSv4.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv4, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV41() throws Exception {
		
		File f = new File("data/acars/ACARSv41.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv41, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV5() throws Exception {
		
		File f = new File("data/acars/ACARSv5.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv5, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV6() throws Exception {
		
		File f = new File("data/acars/ACARSv6.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv6, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV7() throws Exception {
		
		File f = new File("data/acars/ACARSv7.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv7, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV8() throws Exception {
		
		File f = new File("data/acars/ACARSv8.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);		
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv8, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV91() throws Exception {
		
		File f = new File("data/acars/ACARSv91.dat");
		assertTrue(f.exists());
		
		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv91, posdao.getFormat());
		}
	}
	
	@SuppressWarnings("static-method")
	public void testV92() throws Exception {
		
		File f = new File("data/acars/ACARSv92.dat");
		assertTrue(f.exists());

		Compression c = Compression.detect(f);
		assertEquals(Compression.GZIP, c);
		try (InputStream gz = c.getStream(new FileInputStream(f))) {
			GetSerializedPosition posdao = new GetSerializedPosition(gz);
			Collection<? extends RouteEntry> entries = posdao.read();
			assertNotNull(entries);
			assertFalse(entries.isEmpty());
			assertEquals(SerializedDataVersion.ACARSv92, posdao.getFormat());
		}
	}
}