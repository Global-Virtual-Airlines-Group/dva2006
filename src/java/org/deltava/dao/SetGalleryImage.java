// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.gallery.*;

/**
 * A Data Access Object to write Picture Gallery images.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetGalleryImage extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetGalleryImage(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Image to the database.
	 * @param img the Image object, with its image buffer populated
	 * @throws DAOException if a JDBC error occurs
	 * @throws IllegalArgumentException if the buffer is empty
	 */
	public void write(Image img) throws DAOException {
	   if (img.getSize() == 0)
	      throw new IllegalArgumentException("Empty Image Buffer");
	   
		try {
			prepareStatement("INSERT INTO GALLERY (PILOT_ID, NAME, DESCRIPTION, DATE, TYPE, X, Y, SIZE, IMG, FLEET) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setInt(1, img.getAuthorID());
			_ps.setString(2, img.getName());
			_ps.setString(3, img.getDescription());
			_ps.setTimestamp(4, createTimestamp(img.getCreatedOn()));
			_ps.setInt(5, img.getType());
			_ps.setInt(6, img.getWidth());
			_ps.setInt(7, img.getHeight());
			_ps.setInt(8, img.getSize());
			_ps.setBinaryStream(9, img.getInputStream(), img.getSize());
			_ps.setBoolean(10, img.getFleet());

			// Update the database
			executeUpdate(1);

			// Get the new image ID
			img.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates a Gallery Image's name and description.
	 * @param img the Image bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Image img) throws DAOException {
		try {
			prepareStatement("UPDATE GALLERY SET NAME=?, DESCRIPTION=?, FLEET=? WHERE (ID=?)");
			_ps.setString(1, img.getName());
			_ps.setString(2, img.getDescription());
			_ps.setBoolean(3, img.getFleet());
			_ps.setInt(4, img.getID());
			
			// Update the database
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Image Vote to the database. 
	 * @param v the Vote bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Vote v) throws DAOException {
		try {
			prepareStatement("INSERT INTO GALLERYSCORE (IMG_ID, PILOT_ID, SCORE) VALUES (?, ?, ?)");
			_ps.setInt(1, v.getImageID());
			_ps.setInt(2, v.getAuthorID());
			_ps.setInt(3, v.getScore());

			// Add the vote
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Gallery Image from the database. This will automatically remove links from Water Cooler threads.
	 * @param id the Image Database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			startTransaction();
			
			// Delete the Image
			prepareStatement("DELETE FROM GALLERY WHERE (ID=?)");
			_ps.setInt(1, id);

			// Remove the image
			executeUpdate(1);
			
			// Update any Water Cooler threads that link to this.
			prepareStatement("UPDATE common.COOLER_THREADS SET IMAGE_ID=0 WHERE (IMAGE_ID=?)");
			_ps.setInt(1, id);
			executeUpdate(0);
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Image Vote from the database.
	 * @param imgID the Image Database ID
	 * @param pilotID the voting Pilot's Database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int imgID, int pilotID) throws DAOException {
		try {
			prepareStatement("DELETE FROM GALLERYSCORE WHERE (IMG_ID=?) AND (PILOT_ID=?)");
			_ps.setInt(1, imgID);
			_ps.setInt(2, pilotID);

			// Remove the vote
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}