package org.deltava.dao.file;

import java.io.*;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

public class AbstractURLConnectionTestCase extends TestCase {

	protected InputStream _is;
	
	protected void setUp(String fileName) throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.properties");
		_is = new FileInputStream(fileName);
	}
}