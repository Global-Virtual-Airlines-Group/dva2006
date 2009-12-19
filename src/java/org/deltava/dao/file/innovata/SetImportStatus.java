// Copyright 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.WriteBuffer;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to save Innovata Schedule import status.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class SetImportStatus extends WriteBuffer {

	/**
	 * Initializes the Data Access Object.
	 * @param path the directory to save to
	 * @param name the file name
	 */
	public SetImportStatus(String path, String name) {
		super(path, SystemData.get("airline.code") + "." + name);
	}

	/**
	 * Saves the latest Innovata schedule download status.
	 * @param al a Collection of unknown IATA airlinecodes
	 * @param airports a Collection of unknown IATA airport codes
	 * @param eq a Collection of unknown IATA equipment codes
	 * @param msgs any other import messages
	 * @throws DAOException if an I/O error occurs
	 */
	public void write(Collection<String> al, Collection<String> airports, Collection<String> eq, Collection<String> msgs) throws DAOException {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(_f));
			
			// Write messages
			for (String msg : msgs)
				out.println(msg);
			
			// Write airlines
			for (String a : al)
				out.println("airline=" + a);
			
			// Write airports
			for (String ap : airports)
				out.println("airport=" + ap);
			
			// Write equipment
			for (String eqType : eq)
				out.println("eq=" + eqType);
			
			out.close();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}