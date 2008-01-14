// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.deltava.beans.servinfo.Pilot;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to read VATSIM CERT data.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class GetVATSIMData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param is the stream to use
	 */
	public GetVATSIMData(InputStream is) {
		super(is);
	}

	private String getSubstring(String s, char start, char end) {
		int st = s.indexOf(start);
		int en = s.indexOf(end, st);
		return s.substring(++st, en);
	}
	
	private String getNextLine(BufferedReader br) throws IOException {
		String result = br.readLine();
		if (!result.contains("</td>"))
			result = result + br.readLine();
		
		return result.trim();
	}
	
	/**
	 * Returns information about the selected person. The user's e-mail domain is contained within the equipment
	 * code field for a pilot.
	 * @return a Pilot bean
	 * @throws DAOException if an I/O error occurs
	 */
	public Pilot getInfo() throws DAOException {
		try {
			BufferedReader br = getReader();
			
			boolean hasData = false;
			String iData = br.readLine();
			while ((iData != null) && !hasData) {
				hasData = iData.contains("remind.php?");
				if (!hasData)
					iData = br.readLine();
			}
			
			// If we have nothing, then abort
			if (iData == null)
				throw new EOFException("Did not find remind.php");
			
			// Read next few lines - name, email, status, created, location
			if (!iData.contains("<td>"))
				iData = br.readLine();
			
			// Get data
			Pilot p = new Pilot(0);
			p.setName(getSubstring(getNextLine(br), '>', '<').trim() + " XXX");
			p.setEquipmentCode(getSubstring(getNextLine(br), '@', '<').trim());
			p.setComments(getSubstring(getNextLine(br), '>', '<'));
			
			// Return results
			return p;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}