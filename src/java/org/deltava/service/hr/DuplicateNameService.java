// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.hr;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import org.deltava.beans.Person;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.dao.*;

import org.deltava.service.ServiceContext;
import org.deltava.service.ServiceException;
import org.deltava.service.WebService;
import org.deltava.util.StringUtils;

/**
 * A Web Service to check for duplicate pilot names.
 * @author Luke
 * @version 3.4
 * @since 3.1
 */

public class DuplicateNameService extends WebService {

	private class DupeUser extends Person {
		
		DupeUser(String fName, String lName) {
			super(fName, lName);
		}
		
		@Override
		public String getStatusName() {
			return "Invalid";
		}
		
		@Override
		public void addRole(String role) {
			// empty;
		}
		
		@Override
		public boolean isInRole(String roleName) {
			return false;
		}
		
		@Override
		public Collection<String> getRoles() {
			return Collections.emptySet();
		}
		
		@Override
		public String getRowClassName() {
			return null;
		}
	}
	
	/**
	 * Executes the Web Service, returning Pilot names and IDs.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the name
		String fName = ctx.getParameter("fName");
		String lName = ctx.getParameter("lName");
		if (StringUtils.isEmpty(fName) || StringUtils.isEmpty(lName))
			return SC_NO_CONTENT;
		
		// Check if it's a duplicate
		Collection<Integer> dupeResults = new HashSet<Integer>();
		DupeUser usr = new DupeUser(fName, lName);
		usr.setEmail(ctx.getParameter("eMail"));
		try {
			Connection con = ctx.getConnection();
			GetUserData uddao = new GetUserData(con);
			Collection<AirlineInformation> airlines = uddao.getAirlines(true).values();

			// Check for unique name
			GetApplicant adao = new GetApplicant(con);
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			for (AirlineInformation info : airlines) {
				dupeResults.addAll(pdao.checkUnique(usr, info.getDB()));
				dupeResults.addAll(adao.checkUnique(usr, info.getDB()));
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		try {
			ctx.setContentType("text/plain", "UTF-8");
			ctx.println(String.valueOf(dupeResults.size()));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		return SC_OK;
	}
}