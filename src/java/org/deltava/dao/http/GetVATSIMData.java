// Copyright 2007, 2008, 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.*;
import static java.net.HttpURLConnection.HTTP_OK;

import org.jdom2.*;
import org.jdom2.input.*;

import org.deltava.beans.servinfo.Certificate;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read VATSIM CERT data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetVATSIMData extends DAO {

	/**
	 * Returns information about the selected VATSIM certificate.
	 * @param id the VATSIM certificate ID
	 * @return a Certificate bean
	 * @throws DAOException if an error occurs
	 */
	public Certificate getInfo(String id) throws DAOException {
		
		// Get the URL
		String url = SystemData.get("online.vatsim.validation_url") + "?cid=" + id;
		if (StringUtils.isEmpty(id))
			return null;
		
		try {
			init(url);
			if (getResponseCode() != HTTP_OK)
				return null;
			
			// Process the XML document
			Document doc = null;	
			try {
				SAXBuilder builder = new SAXBuilder();
				doc = builder.build(getIn());
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
			String cid = ue.getAttributeValue("cid");
			if (StringUtils.isEmpty(cid))
				return null;
			
			// Check if inactive
			String rating = ue.getChildTextTrim("rating");
			boolean isInactive = "suspended".equalsIgnoreCase(rating) || "inactive".equalsIgnoreCase(rating);
			
			// Create the return object
			Certificate c = new Certificate(StringUtils.parse(id, 0));
			c.setFirstName(ue.getChildTextTrim("name_first"));
			c.setLastName(ue.getChildTextTrim("name_last"));
			c.setEmailDomain(ue.getChildTextTrim("email"));
			c.setRegistrationDate(StringUtils.parseInstant(ue.getChildTextTrim("regdate"), "yyyy-MM-dd HH:mm:ss"));
			c.setActive(!isInactive);
			
			// Load Pilot ratings
			Collection<String> ratings = new LinkedHashSet<String>(StringUtils.split(ue.getChildTextTrim("pilotrating"), ","));
			for (String pRating : ratings)
				c.addPilotRating(pRating);
			
			return c;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}