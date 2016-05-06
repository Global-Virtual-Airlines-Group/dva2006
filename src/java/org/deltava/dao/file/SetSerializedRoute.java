// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.Collection;

import org.deltava.beans.navdata.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to serialize route data.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class SetSerializedRoute extends WriteableDAO {

	/**
	 * Initializes the Data Access Object.
	 * @param os the OutputStream to write to
	 */
	public SetSerializedRoute(OutputStream os) {
		super(os);
	}
	
	/**
	 * Serializes simFDR Route records. This will NOT write SID/STAR waypoint entries.
	 * @param flightID the Flight ID
	 * @param entries a Collection of NavigationDataBean beans
	 * @throws DAOException if an I/O error occurs
	 */
	public void archive(int flightID, Collection<NavigationDataBean> entries) throws DAOException {
		if (entries.isEmpty()) return;
		try (DataOutputStream out = new DataOutputStream(_os)) {
			out.writeShort(2);
			out.writeInt(flightID);
			out.writeInt(entries.size());
			for (NavigationDataBean nd : entries) {
				out.writeShort(nd.getType().ordinal());
				out.writeDouble(nd.getLatitude());
				out.writeDouble(nd.getLongitude());
				out.writeUTF(nd.getCode());
				if (nd.getType() != Navaid.INT)
					out.writeUTF((nd.getName() == null) ? "" : nd.getName());
				out.writeUTF((nd.getAirway() == null) ? "" : nd.getAirway());
				if (nd instanceof NavigationFrequencyBean)
					out.writeUTF(((NavigationFrequencyBean) nd).getFrequency()); 
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}