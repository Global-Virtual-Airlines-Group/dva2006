// Copyright 2007, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.Properties;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to save a Java properties file.
 * @author Luke
 * @version 4.1
 * @since 1.0
 */

public class SetProperties extends WriteableDAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param os the OutputStream to write to
	 */
	public SetProperties(OutputStream os) {
		super(os);
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