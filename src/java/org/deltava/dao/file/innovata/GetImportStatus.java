// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.DAO;

/**
 * A Data Access Object to load Innovata Schedule import status. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetImportStatus extends DAO {
	
	private final Collection<String> _airports = new LinkedHashSet<String>();
	private final Collection<String> _eq = new LinkedHashSet<String>();
	private final Collection<String> _msgs = new ArrayList<String>();

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetImportStatus(InputStream is) {
		super(is);
	}
	
	/**
	 * Loads import status.
	 * @throws DAOException if an I/O error occurs
	 */
	public void load() throws DAOException {
		try {
			BufferedReader br = getReader();
			while (br.ready()) {
				String data = br.readLine();
				StringTokenizer tkns = new StringTokenizer(data, "=");
				if (tkns.countTokens() == 2) {
					String code = tkns.nextToken();
					if ("airport".equals(code))
						_airports.add(tkns.nextToken().toUpperCase());
					else if ("eq".equals(code))
						_eq.add(tkns.nextToken().toUpperCase());
				} else
					_msgs.add(data);
			}
			
			br.close();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}

	/**
	 * Returns all invalid airport codes from the previous import.
	 * @return a Collection of IATA airport codes
	 */
	public Collection<String> getUnknownAirports() {
		return _airports;
	}
	
	/**
	 * Returns all invalid equipment codes from the previous import.
	 * @return a Collection of ICAO equipment codes
	 */
	public Collection<String> getUnknownEquipment() {
		return _eq;
	}
	
	/**
	 * Returns all other import messages from the previous import.
	 * @return a Collection of messags
	 */
	public Collection<String> getMessages() {
		return _msgs;
	}
}