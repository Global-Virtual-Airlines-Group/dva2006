// Copyright 2006, 2009, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to load Innovata Schedule import status. 
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetImportStatus extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetImportStatus(InputStream is) {
		super(is);
	}
	
	/**
	 * Loads import status.
	 * @return an ImportStatus bean
	 * @throws DAOException if an I/O error occurs
	 */
	public ImportStatus load() throws DAOException {
		try (BufferedReader br = getReader()) {
			String data = br.readLine();
			StringTokenizer tkns = new StringTokenizer(data, "=");
			if (!"src".equals(tkns.nextToken()))
				throw new IllegalArgumentException("Invalid source - " + data);
			
			ScheduleSource src = ScheduleSource.valueOf(tkns.nextToken());
			data = br.readLine();
			tkns = new StringTokenizer(data, "=");
			if (!"date".equals(tkns.nextToken()))
				throw new IllegalArgumentException("Invalid date - " + data);
			
			ImportStatus st = new ImportStatus(src, Instant.ofEpochMilli(Long.parseLong(tkns.nextToken()))); 
			while (br.ready()) {
				data = br.readLine();
				tkns = new StringTokenizer(data, "=");
				if (tkns.countTokens() == 2) {
					String code = tkns.nextToken().toLowerCase();
					switch (code) {
					case "airport":
						st.addInvalidAirport(tkns.nextToken().toUpperCase());
						break;
						
					case "airline":
						st.addInvalidAirline(tkns.nextToken().toUpperCase());
						break;
						
					case "eq":
						st.addInvalidEquipment(tkns.nextToken().toUpperCase());
						break;
						
					default:
						throw new IllegalArgumentException("Invalid status code - " + code);
					}
				} else
					st.addMessage(data);
			}
			
			return st;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}