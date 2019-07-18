// Copyright 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.deltava.beans.acars.ArchivedRoute;
import org.deltava.beans.navdata.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to serialize route data.
 * @author Luke
 * @version 8.6
 * @since 7.0
 */

public class SetSerializedRoute extends WriteableDAO {
	
	private static final short DATA_VERSION = 3;

	/**
	 * Initializes the Data Access Object.
	 * @param os the OutputStream to write to
	 */
	public SetSerializedRoute(OutputStream os) {
		super(os);
	}
	
	/**
	 * Serializes ACARS Route records. This will NOT write SID/STAR waypoint entries.
	 * @param rt the ArchivedRoute bean
	 * @throws DAOException if an I/O error occurs
	 */
	public void archive(ArchivedRoute rt) throws DAOException {
		if (rt.getSize() == 0) return;
		try (DataOutputStream out = new DataOutputStream(_os)) {
			out.writeShort(DATA_VERSION);
			out.writeInt(rt.getID());
			out.writeInt(rt.getAIRACVersion());
			out.writeInt(rt.getSize());
			for (NavigationDataBean nd : rt.getWaypoints()) {
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