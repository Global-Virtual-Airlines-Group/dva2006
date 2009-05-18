// Copyright 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.jdom.*;
import org.jdom.input.*;

import org.deltava.beans.servinfo.Certificate;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to read VATSIM CERT data.
 * @author Luke
 * @version 2.6
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

	/**
	 * Returns information about the selected VATSIM certificate.
	 * @return a Certificate bean
	 * @throws DAOException if an error occurs
	 */
	public Certificate getInfo() throws DAOException {
		try {
			// Process the XML document
			Document doc = null;	
			try {
				SAXBuilder builder = new SAXBuilder();
				doc = builder.build(getReader());
			} catch (JDOMException je) {
				throw new DAOException(je);
			}
			
			// Get data
			Element re = doc.getRootElement();
			if ((re == null) || (!"root".equals(re.getName())))
				throw new DAOException("root element expected");
			
			// Get user element
			Element ue = re.getChild("user");
			if (ue == null)
				throw new DAOException("user element expected");
			
			// Check the ID
			String id = ue.getAttributeValue("cid");
			if (StringUtils.isEmpty(id))
				return null;
			
			// Check if inactive
			String rating = ue.getChildTextTrim("rating");
			boolean isInactive = "suspended".equalsIgnoreCase(rating) || "inactive".equalsIgnoreCase(rating);
			
			// Create the return object
			Certificate c = new Certificate(StringUtils.parse(id, 0));
			c.setFirstName(ue.getChildTextTrim("name_first"));
			c.setLastName(ue.getChildTextTrim("name_last"));
			c.setEmailDomain(ue.getChildTextTrim("email"));
			c.setRegistrationDate(StringUtils.parseDate(ue.getChildTextTrim("regdate"), "yyyy-MM-dd HH:mm:ss"));
			c.setActive(!isInactive);
			return c;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}