// Copyright 2005, 2006, 2007, 2009, 2011, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;
import java.time.ZonedDateTime;

import org.deltava.beans.gallery.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Image Gallery data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetGallery extends DAO {

	/**
	 * Initializes the DAO with a given JDBC connection.
	 * @param c the JDBC Connection
	 */
	public GetGallery(Connection c) {
		super(c);
	}

	/**
	 * Returns the metadata associated with a particular Gallery image in the current database.
	 * @param id the Image id
	 * @return an Image, or null if the id was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Image getImageData(int id) throws DAOException {
		return getImageData(id, SystemData.get("airline.db"));
	}
	
	/**
	 * Returns the metadata associated with a particular Gallery image.
	 * @param id the Image id
	 * @param dbName the database name
	 * @return an Image, or null if the id was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Image getImageData(int id, String dbName) throws DAOException {
		
		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT G.NAME, G.DESCRIPTION, G.TYPE, G.X, G.Y, G.SIZE, G.DATE, G.FLEET, G.PILOT_ID, T.ID FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".GALLERY G LEFT JOIN common.COOLER_THREADS T ON (G.ID=T.IMAGE_ID) WHERE (G.ID=?) LIMIT 1");
		
		try {
			Image img = null;
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						img = new Image(rs.getString(1), rs.getString(2));
						img.setID(id);
						img.setType(rs.getInt(3));
						img.setWidth(rs.getInt(4));
						img.setHeight(rs.getInt(5));
						img.setSize(rs.getInt(6));
						img.setCreatedOn(rs.getTimestamp(7).toInstant());
						img.setFleet(rs.getBoolean(8));
						img.setAuthorID(rs.getInt(9));
						img.setThreadID(rs.getInt(10));
					}
				}
			}

			if (img == null)
				return null;

			// Load gallery image votes
			sqlBuf = new StringBuilder("SELECT PILOT_ID FROM ");
			sqlBuf.append(db);
			sqlBuf.append(".GALLERYSCORE WHERE (IMG_ID=?)");
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						img.addLike(rs.getInt(1));
				}
			}

			return img;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all the image metadata associated with the Fleet Gallery. <i>No vote data is returned </i>.
	 * @return a List of Images in the Fleet Gallery
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Image> getFleetGallery() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT I.NAME, I.DESCRIPTION, I.ID, I.PILOT_ID, I.DATE, I.FLEET, I.TYPE, I.X, I.Y, I.SIZE, "
			+ "(SELECT COUNT(PILOT_ID) FROM GALLERYSCORE WHERE (IMG_ID=I.ID)) AS LC FROM GALLERY I WHERE (I.FLEET=?) ORDER BY I.NAME")) {
			ps.setBoolean(1, true);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retusn Images in the Image Gallery created on a specific date.
	 * @param dt the date the image was posted
	 * @return a List of Image beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Image> getPictureGallery(java.time.Instant dt) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT I.NAME, I.DESCRIPTION, I.ID, I.PILOT_ID, I.DATE, I.FLEET, I.TYPE, I.X, I.Y, I.SIZE, "
			+ "(SELECT COUNT(PILOT_ID) FROM GALLERYSCORE WHERE (IMG_ID=I.ID)) AS LC FROM GALLERY I WHERE (DATE(I.DATE)=DATE(?)) ORDER BY I.DATE")) {
			ps.setTimestamp(1, createTimestamp(dt));
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns Images in the Image Gallery. This can optionally select a month's worth of Images. 
	 * @param orderBy the SQL ORDER BY clause
	 * @param month the optional month name, in &quot;MMMM YYYY&quot; format
	 * @return a Collection of Image beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Image> getPictureGallery(String orderBy, String month) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT I.NAME, I.DESCRIPTION, I.ID, I.PILOT_ID, I.DATE, I.FLEET, I.TYPE, I.X, "
			+ "I.Y, I.SIZE, (SELECT COUNT(PILOT_ID) FROM GALLERYSCORE WHERE (IMG_ID=I.ID)) AS LC FROM GALLERY I ");

		// Append the month query if present
		if (month != null)
			sqlBuf.append("WHERE (MONTHNAME(I.DATE)=?) AND (YEAR(I.DATE)=?) ");

		sqlBuf.append("ORDER BY ");
		sqlBuf.append(orderBy);

		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			if (month != null) {
				StringTokenizer tkns = new StringTokenizer(month, " ");
				ps.setString(1, tkns.nextToken());
				ps.setInt(2, StringUtils.parse(tkns.nextToken(), ZonedDateTime.now().getYear()));
			}

			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads the Months with Images in the Gallery.
	 * @return a Collection of Month/Year values 
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getMonths() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DISTINCT CONCAT_WS(' ', MONTHNAME(DATE), YEAR(DATE)) FROM GALLERY ORDER BY DATE DESC")) {
			Collection<String> results = new LinkedHashSet<String>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to parse Image result sets.
	 */
	private static List<Image> execute(PreparedStatement ps) throws SQLException {
		List<Image> results = new ArrayList<Image>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasLikes = (rs.getMetaData().getColumnCount() > 10);
			boolean hasThreadInfo = (rs.getMetaData().getColumnCount() > 11); 
			while (rs.next()) {
				Image img = new Image(rs.getString(1), rs.getString(2));
				img.setID(rs.getInt(3));
				img.setAuthorID(rs.getInt(4));
				img.setCreatedOn(rs.getTimestamp(5).toInstant());
				img.setFleet(rs.getBoolean(6));
				img.setType(rs.getInt(7));
				img.setWidth(rs.getInt(8));
				img.setHeight(rs.getInt(9));
				img.setSize(rs.getInt(10));
				if (hasLikes)
					img.setLikeCount(rs.getInt(11));
				if (hasThreadInfo)
					img.setThreadID(rs.getInt(12));

				results.add(img);
			}
		}

		return results;
	}
}