// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.Collection;

import org.deltava.beans.servinfo.PositionData;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to serialize online track data.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class SetSerializedOnline extends WriteableDAO {

	/**
	 * Initializes the Data Access Object.
	 * @param os the OutputStream to write to
	 */
	public SetSerializedOnline(OutputStream os) {
		super(os);
	}
	
	/**
	 * Serializes simFDR Online data records.
	 * @param flightID the Flight ID
	 * @param entries a Collection of PositionData beans
	 * @throws DAOException if an I/O error occurs
	 */
	public void archive(int flightID, Collection<PositionData> entries) throws DAOException {
		if (entries.isEmpty()) return;
		try (DataOutputStream out = new DataOutputStream(_os)) {
			out.writeShort(1);
			out.writeInt(flightID);
			out.writeInt(entries.size());
			for (PositionData pd : entries) {
				out.writeLong(pd.getDate().toEpochMilli());
				out.writeDouble(pd.getLatitude());
				out.writeDouble(pd.getLongitude());
				out.writeInt(pd.getAltitude());
				out.writeInt(pd.getAirSpeed());
				out.writeShort(pd.getHeading());
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}