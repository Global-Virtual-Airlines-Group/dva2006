// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;

import org.json.*;
import org.apache.log4j.Logger;

import org.deltava.beans.servinfo.*;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A Data Access Object to read VATSIM Authorized Training Organization data.
 * @author Luke
 * @version 7.2
 * @since 7.2
 */

public class GetATOData extends DAO {
	
	private static final Logger log = Logger.getLogger(GetATOData.class);
	
	private static final Cache<CacheableCollection<PilotRating>> _rCache = CacheManager.getCollection(PilotRating.class, "ATORatings");
	private static final Cache<CacheableCollection<Certificate>> _insCache = CacheManager.getCollection(Certificate.class, "ATOInstructors");
	
	/**
	 * Returns information about a Pilot's Certifications.
	 * @param cid the Pilot's certificate ID
	 * @return a Collection of Certificate beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<PilotRating> get(String cid) throws DAOException {
		int id = StringUtils.parse(cid, 0);
		return getCertificates().stream().filter(c -> (c.getID() == id)).collect(Collectors.toList());
	}
	
	/**
	 * Returns information about an ATO instructor.
	 * @param cid the Instructor's certificate ID
	 * @return a Collection of Certificate beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<Certificate> getInstructor(String cid) throws DAOException {
		int id = StringUtils.parse(cid, 0);
		return getInstructors().stream().filter(c -> (c.getID() == id)).collect(Collectors.toList());
	}

	/**
	 * Retrieves all ATO Instructors.
	 * @return a Collection of Certificate beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<Certificate> getInstructors() throws DAOException {
		
		// Check the cache
		CacheableCollection<Certificate> results = _insCache.get(Certificate.class);
		if (results != null)
			return results.clone();
		
		try {
			init("https://ptd.vatsim.net/api/instructors.json");
			if (getResponseCode() != HTTP_OK)
				return Collections.emptyList();

			// Parse the JSON
			JSONArray ja = new JSONArray(new JSONTokener(getIn()));
			results = new CacheableList<Certificate>(Certificate.class);
			for (int x = 0; x < ja.length(); x++) {
				JSONObject co = ja.getJSONObject(x);
				JSONArray cro = co.getJSONArray("Roles");
				Certificate c = new Certificate(co.getInt("cid"));
				c.setName(co.getString("name").replace(" (" + c.getID() + ")", ""));
				
				// Parse roles
				boolean isActive = true;
				for (int y = 0; isActive && (y < cro.length()); y++)
					isActive &= !cro.getJSONObject(y).getString("name").equals("Inactive");
				
				c.setActive(isActive);
				results.add(c);
			}

			_insCache.add(results);
			return results.clone();
		} catch (SocketTimeoutException ste) {
			log.warn("VATSIM Instructors - " +  ste.getMessage());
			return Collections.emptySet();
		} catch (IOException ie) {
			throw new HTTPDAOException(ie);
		}
	}

	/**
	 * Returns all Pilot certifications.
	 * @return a Collection of Certificate beans
	 * @throws DAOException if an error occurs
	 */
	public Collection<PilotRating> getCertificates() throws DAOException {
		
		// Check the cache
		CacheableCollection<PilotRating> results = _rCache.get(PilotRating.class);
		if (results != null)
			return results.clone();
		
		try {
			init("https://ptd.vatsim.net/api/certifications.json");
			if (getResponseCode() != HTTP_OK)
				return Collections.emptyList();

			// Parse the JSON
			JSONArray ja = new JSONArray(new JSONTokener(getIn()));
			results = new CacheableList<PilotRating>(PilotRating.class);
			for (int x = 0; x < ja.length(); x++) {
				JSONObject co = ja.getJSONObject(x);
				String cid = co.optString("pilot_name", null);
				if (cid == null) continue;
				
				JSONObject ao = co.getJSONObject("ato");
				int id = StringUtils.parse(cid.substring(cid.lastIndexOf('(') + 1, cid.lastIndexOf(')')), 0);
				PilotRating r = new PilotRating(id, co.getJSONObject("rating").getString("name"));
				r.setName(cid.substring(0, cid.indexOf(" (")));
				r.setIssueDate(Instant.parse(co.getString("created_at")));
				r.setInstructorID(co.getJSONObject("user").getInt("cid"));
				r.setATO(ao.getString("name"), ao.getString("url"));
				results.add(r);
			}

			_rCache.add(results);
			return results.clone();
		} catch (SocketTimeoutException ste) {
			log.warn("VATSIM Certificates - " +  ste.getMessage());
			return Collections.emptySet();
		} catch (IOException ie) {
			throw new HTTPDAOException(ie);
		}
	}
}