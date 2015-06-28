// Copyright 2006, 2007, 2009, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.WriteBuffer;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to save Innovata Schedule import status.
 * @author Luke
 * @version 6.0
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
		try (PrintWriter out = new PrintWriter(_f)) {
			msgs.forEach(msg -> out.println(msg));			// messages
			al.forEach(a -> out.println("airline=" + a));		// airlines
			airports.forEach(a -> out.println("airport=" + a)); // airports
			eq.forEach(eqType -> out.println("eq=" + eqType)); // equipment
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}