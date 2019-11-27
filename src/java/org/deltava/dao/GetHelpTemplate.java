// Copyright 2010, 2011, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.help.ResponseTemplate;

/**
 * A Data Access Object for Help Desk response templates. 
 * @author Luke
 * @version 9.0
 * @since 3.2
 */

public class GetHelpTemplate extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetHelpTemplate(Connection c) {
		super(c);
	}

	/**
	 * Retrieves a Help Desk response template.
	 * @param title the template title
	 * @return a ResponseTemplate bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ResponseTemplate get(String title) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT TITLE, BODY FROM HELPDESK_RSPTMP WHERE (TITLE=?) LIMIT 1")) {
			ps.setString(1, title);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Retrieves all Help Desk response templates.
	 * @return a Collection of ResponseTemplate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ResponseTemplate> getAll() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT TITLE, BODY FROM HELPDESK_RSPTMP ORDER BY TITLE")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
	
	/*
	 * Helper method to parse response template result sets.
	 */
	private static List<ResponseTemplate> execute(PreparedStatement ps) throws SQLException {
		List<ResponseTemplate> results = new ArrayList<ResponseTemplate>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				ResponseTemplate tmp = new ResponseTemplate();
				tmp.setTitle(rs.getString(1));
				tmp.setBody(rs.getString(2));
				results.add(tmp);
			}
		}
		
		return results;
	}
}