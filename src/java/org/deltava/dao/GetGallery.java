// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.gallery.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Image Gallery data.
 * @author Luke
 * @version 1.0
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
	 * Returns the metadata associated with a particular Gallery image in the current database. <i>No vote data is returned </i>.
	 * @param id the Image id
	 * @return an Image, or null if the id was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Image getImageData(int id) throws DAOException {
		return getImageData(id, SystemData.get("airline.db"));
	}
	
	/**
	 * Returns the metadata associated with a particular Gallery image. <i>No vote data is returned </i>.
	 * @param id the Image id
	 * @param dbName the database name
	 * @return an Image, or null if the id was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Image getImageData(int id, String dbName) throws DAOException {
		
		// Build the SQL statement
		dbName = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT G.NAME, G.DESCRIPTION, G.TYPE, G.X, G.Y, G.SIZE, G.DATE, G.FLEET, "
				+ "G.PILOT_ID, T.ID FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append(".GALLERY G LEFT JOIN common.COOLER_THREADS T ON (G.ID=T.IMAGE_ID) WHERE (G.ID=?)");
		
		try {
			setQueryMax(1);
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, id);

			// Execute the query - return null if no image found
			setQueryMax(0);
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return null;
			}

			// Create the image bean
			Image img = new Image(rs.getString(1), rs.getString(2));
			img.setID(id);
			img.setType(rs.getInt(3));
			img.setWidth(rs.getInt(4));
			img.setHeight(rs.getInt(5));
			img.setSize(rs.getInt(6));
			img.setCreatedOn(rs.getTimestamp(7));
			img.setFleet(rs.getBoolean(8));
			img.setAuthorID(rs.getInt(9));
			img.setThreadID(rs.getInt(10));

			// Clean up
			rs.close();
			_ps.close();

			// Load gallery image votes
			sqlBuf = new StringBuilder("SELECT * FROM ");
			sqlBuf.append(dbName);
			sqlBuf.append(".GALLERYSCORE WHERE (IMG_ID=?)");
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setInt(1, id);
			rs = _ps.executeQuery();
			while (rs.next())
				img.addVote(new Vote(rs.getInt(2), rs.getInt(3), rs.getInt(1)));

			// Clean up and return
			rs.close();
			_ps.close();
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
		try {
			prepareStatement("SELECT I.NAME, I.DESCRIPTION, I.ID, I.PILOT_ID, I.DATE, I.FLEET, I.TYPE, I.X, I.Y, I.SIZE, " +
					"COUNT(V.SCORE) AS VC, AVG(V.SCORE) AS SC FROM GALLERY I LEFT JOIN GALLERYSCORE V ON " +
					"(I.ID=V.IMG_ID) WHERE (I.FLEET=?) GROUP BY I.ID ORDER BY I.NAME");
			_ps.setBoolean(1, true);

			// Execute the query
			return execute();
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
				+ "I.Y, I.SIZE, COUNT(V.SCORE) AS VC, AVG(V.SCORE) AS SC FROM GALLERY I LEFT JOIN GALLERYSCORE V ON "
				+ "(I.ID=V.IMG_ID) ");

		// Append the month query if present
		if (month != null)
			sqlBuf.append("WHERE (MONTHNAME(I.DATE)=?) AND (YEAR(I.DATE)=?) ");

		sqlBuf.append("GROUP BY I.ID ORDER BY ");
		sqlBuf.append(orderBy);

		try {
			prepareStatement(sqlBuf.toString());
			if (month != null) {
				StringTokenizer tkns = new StringTokenizer(month, " ");
				_ps.setString(1, tkns.nextToken());
				_ps.setInt(2, StringUtils.parse(tkns.nextToken(), Calendar.getInstance().get(Calendar.YEAR)));
			}

			// Execute the query
			return execute();
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
		try {
			prepareStatement("SELECT DISTINCT CONCAT_WS(' ', MONTHNAME(DATE), YEAR(DATE)) FROM GALLERY "
					+ "ORDER BY DATE DESC");

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			Collection<String> results = new LinkedHashSet<String>();
			while (rs.next())
				results.add(rs.getString(1));

			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse Image result sets.
	 */
	private List<Image> execute() throws SQLException {
		ResultSet rs = _ps.executeQuery();
		boolean hasVotes = (rs.getMetaData().getColumnCount() > 11);
		boolean hasThreadInfo = (rs.getMetaData().getColumnCount() > 12); 

		// Iterate through the results
		List<Image> results = new ArrayList<Image>();
		while (rs.next()) {
			Image img = new Image(rs.getString(1), rs.getString(2));
			img.setID(rs.getInt(3));
			img.setAuthorID(rs.getInt(4));
			img.setCreatedOn(rs.getTimestamp(5));
			img.setFleet(rs.getBoolean(6));
			img.setType(rs.getInt(7));
			img.setWidth(rs.getInt(8));
			img.setHeight(rs.getInt(9));
			img.setSize(rs.getInt(10));
			if (hasVotes) {
				img.setVoteCount(rs.getInt(11));
				img.setScore(rs.getDouble(12));
			}
			
			if (hasThreadInfo)
				img.setThreadID(rs.getInt(13));

			// Add to results
			results.add(img);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}