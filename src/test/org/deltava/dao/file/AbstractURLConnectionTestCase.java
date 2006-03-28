// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao.file;

import java.net.*;

import org.apache.log4j.PropertyConfigurator;
import org.deltava.dao.file.FileURLConnection;

import junit.framework.TestCase;

public class AbstractURLConnectionTestCase extends TestCase {

	protected URLConnection _con;
	
	protected void setUp(String fileName) throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.properties");
		_con = new FileURLConnection(fileName);
		_con.connect();
	}
	
	protected void tearDown() throws Exception {
		_con = null;
		super.tearDown();
	}
}