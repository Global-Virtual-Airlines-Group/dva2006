// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.Properties;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to read properties files.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetProperties extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read from
	 */
	public GetProperties(InputStream is) {
		super(is);
	}

	/**
	 * Loads the Properties.
	 * @return a Properties object
	 * @throws DAOException if an I/O error occurs
	 */
	public Properties read() throws DAOException {
		try {
			Properties p = new Properties();
			p.load(getReader());
			return p;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}