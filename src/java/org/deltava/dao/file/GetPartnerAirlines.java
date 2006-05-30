// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Partner airline data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPartnerAirlines extends DAO {
	
	private static final Logger log = Logger.getLogger(GetPartnerAirlines.class);

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream containing the data
	 */
	public GetPartnerAirlines(InputStream is) {
		super(is);
	}

	/**
	 * Loads the Partner airline data.
	 * @return a Collection of PartnerAirline beans
	 * @throws DAOException if an I/O error occurs
	 */
	public Collection<PartnerAirline> getPartners() throws DAOException {
		Collection<PartnerAirline> results = new ArrayList<PartnerAirline>();
		
		try {
			LineNumberReader br = new LineNumberReader(getReader());
			while (br.ready()) {
				String data = br.readLine();
				StringTokenizer tkns = new StringTokenizer(data, ",");
				if ((!data.startsWith(";")) && (tkns.countTokens() >= 4)) {
					String aCode = tkns.nextToken();
					Airline a = SystemData.getAirline(aCode);
					if (PartnerAirline.IGNORE.equals(aCode)) {
						try {
							PartnerAirline pa = new PartnerAirline(PartnerAirline.IGNORE.getAirline(),
									Integer.parseInt(tkns.nextToken()), Integer.parseInt(tkns.nextToken()), tkns.nextToken());
							results.add(pa);
						} catch (NumberFormatException nfe) {
							log.warn("Invalid data at Line " + br.getLineNumber() + " - " + data);
						}
					} else if (a == null)
						log.warn("Unknown Airline code " + aCode + " at Line " + br.getLineNumber());
					else {
						try {
							PartnerAirline pa = new PartnerAirline(a, Integer.parseInt(tkns.nextToken()),
									Integer.parseInt(tkns.nextToken()), tkns.nextToken());
							results.add(pa);
						} catch (NumberFormatException nfe) {
							log.warn("Invalid data at Line " + br.getLineNumber() + " - " + data);
						}
					}
				}
			}
			
			br.close();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		// Return resutls
		return results;
	}
}