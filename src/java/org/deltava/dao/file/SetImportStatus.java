// Copyright 2006, 2007, 2009, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.deltava.beans.schedule.ImportStatus;

import org.deltava.dao.DAOException;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to save raw schedule import status.
 * @author Luke
 * @version 9.0
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
	 * Saves the raw schedule download status.
	 * @param st an ImportStatus bean
	 * @throws DAOException if an I/O error occurs
	 */
	public void write(ImportStatus st) throws DAOException {
		try (PrintWriter out = new PrintWriter(_f)) {
			out.println("src=" + st.getSource().name());
			out.println("date=" + st.getImportDate().toEpochMilli());
			st.getErrorMessages().forEach(msg -> out.println(msg));
			st.getInvalidAirlines().forEach(a -> out.println("airline=" + a));
			st.getInvalidAirports().forEach(a -> out.println("airport=" + a));
			st.getInvalidEquipment().forEach(eqType -> out.println("eq=" + eqType));
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}