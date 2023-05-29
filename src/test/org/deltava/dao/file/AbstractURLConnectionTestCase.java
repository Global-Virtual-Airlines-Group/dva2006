package org.deltava.dao.file;

import java.io.*;

import junit.framework.TestCase;

public class AbstractURLConnectionTestCase extends TestCase {

	protected InputStream _is;
	
	protected void setUp(String fileName) throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		_is = new FileInputStream(fileName);
	}
}