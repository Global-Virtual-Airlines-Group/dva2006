// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.Properties;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to save a Java properties file.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetProperties {
	
	private OutputStream _os;

	/**
	 * Initializes the Data Access Object.
	 * @param os the OutputStream to write to
	 */
	public SetProperties(OutputStream os) {
		super();
		_os = os;
	}

	/**
	 * Saves the properties.
	 * @param p the Properties bean to write
	 * @param comment the header comment or null if none
	 * @throws DAOException if an I/O error occurs
	 */
	public void save(Properties p, String comment) throws DAOException {
		try {
			p.store(_os, comment);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}