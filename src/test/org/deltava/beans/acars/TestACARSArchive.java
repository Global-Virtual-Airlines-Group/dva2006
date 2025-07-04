// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.io.*;
import java.util.zip.*;

import org.apache.logging.log4j.*;

import junit.framework.TestCase;

public class TestACARSArchive extends TestCase {

	private Logger log;

	private byte[] _zip;
	private byte[] _xml;
	private byte[] _sha;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Init Log4j
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(TestACARSArchive.class);

		File f = new File("data/acars/ACARS Flight O-2006070220.xml");
		assertTrue(f.exists());
		_xml = new byte[(int) f.length()];
		try (InputStream is = new FileInputStream(f)) {
			is.read(_xml);
		}

		f = new File("data/acars/ACARS Flight O-2006070220.sha");
		assertTrue(f.exists());
		_sha = new byte[(int) f.length()];
		try (InputStream is = new FileInputStream(f)) {
			is.read(_sha);
		}

		f = new File("data/acars/flight.zip");
		assertTrue(f.exists());
		_zip = new byte[(int) f.length()];
		try (InputStream is = new FileInputStream(f)) {
			is.read(_zip);
		}
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
		String zipXML = null; String zipSHA = null;
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(_zip))) {
			assertNotNull(zis);
			ZipEntry ze = zis.getNextEntry();
			assertNotNull(ze);

			// Load the data
			while ((ze != null) && ((zipSHA == null) || (zipXML == null))) {
				String name = ze.getName().toLowerCase();
				byte[] buf = new byte[(int) ze.getSize()];
				assertTrue(zis.read(buf, 0, buf.length) > 0);
				if (name.endsWith(".xml")) {
					zipXML = new String(buf, "UTF-8");
					// zipXML = xml.substring(0, xml.length() - 1);
				} else if (name.endsWith(".sha"))
					zipSHA = new String(buf, "UTF-8").trim();

				ze = zis.getNextEntry();
			}
		}

		assertNotNull(zipXML);
		assertNotNull(zipSHA);
		log.info("xml = " + xml.length() + ", zipXML = " + zipXML.length());
		// assertEquals(xml.length(), zipXML.length());

		// Check the end
		int x = 0;
		for (x = 0; x < zipXML.length(); x++) {
			String c1 = xml.substring(x, x + 1);
			String c2 = zipXML.substring(x, x + 1);
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