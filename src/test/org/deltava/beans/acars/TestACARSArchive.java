// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.io.*;
import java.util.zip.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

public class TestACARSArchive extends TestCase {
	
	private Logger log;
	
	private byte[] _zip;
	private byte[] _xml;
	private byte[] _sha;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		log = Logger.getLogger(TestACARSArchive.class);
		
		File f = new File("data/acars/ACARS Flight O-2006070220.xml");
		assertTrue(f.exists());
		_xml = new byte[(int) f.length()];
		InputStream is = new FileInputStream(f);
		is.read(_xml);
		is.close();
		
		f = new File("data/acars/ACARS Flight O-2006070220.sha");
		assertTrue(f.exists());
		_sha = new byte[(int) f.length()];
		is = new FileInputStream(f);
		is.read(_sha);
		is.close();
		
		f = new File("data/acars/flight.zip");
		assertTrue(f.exists());
		_zip = new byte[(int) f.length()];
		is = new FileInputStream(f);
		is.read(_zip);
		is.close();
	}
	
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testSHA() throws Exception {
		
		// Get the XML
		String xml = new String(_xml);
		xml = xml.substring(0, xml.length() - 2);
		assertNotNull(xml);
		
		// Get the SHA
		String sha = new String(_sha, "UTF-8").trim();
		assertNotNull(sha);
		
		// Open the ZIP file
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(_zip));
		assertNotNull(zis);
		ZipEntry ze = zis.getNextEntry();
		assertNotNull(ze);
		
		// Load the data
		String zipXML = null; String zipSHA = null;
		while ((ze != null) && ((zipSHA == null) || (zipXML == null))) {
			String name = ze.getName().toLowerCase();
			byte[] buf = new byte[(int) ze.getSize()];
			zis.read(buf, 0, buf.length);
			if (name.endsWith(".xml")) {
				zipXML = new String(buf, "UTF-8");
				//zipXML = xml.substring(0, xml.length() - 1);
			} else if (name.endsWith(".sha"))
				zipSHA = new String(buf, "UTF-8").trim();
			
			ze = zis.getNextEntry();
		}
		
		zis.close();
		assertNotNull(zipXML);
		assertNotNull(zipSHA);
		log.info("xml = " + xml.length() + ", zipXML = " + zipXML.length());
		//assertEquals(xml.length(), zipXML.length());
		
		// Check the end
		int x = 0;
		for (x = 0; x < zipXML.length(); x++) {
			String c1 = xml.substring(x, x+1);
			String c2 = zipXML.substring(x, x+1);
			if (!c1.equals(c2)) {
				log.info(xml.substring(x - 12) + " ]");
				log.info(zipXML.substring(x - 12) + " ]");
				break;
			}
		}
		
		// Compare the data
		assertEquals(sha, zipSHA);
		assertEquals(xml, zipXML);
	}
}