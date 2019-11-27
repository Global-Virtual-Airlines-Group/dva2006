// Copyright 2005, 2006, 2007, 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.gallery.*;

/**
 * A Data Access Object to write Picture Gallery images.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepare("INSERT INTO GALLERY (PILOT_ID, NAME, DESCRIPTION, DATE, TYPE, X, Y, SIZE, IMG, FLEET) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setInt(1, img.getAuthorID());
				ps.setString(2, img.getName());
				ps.setString(3, img.getDescription());
				ps.setTimestamp(4, createTimestamp(img.getCreatedOn()));
				ps.setInt(5, img.getType());
				ps.setInt(6, img.getWidth());
				ps.setInt(7, img.getHeight());
				ps.setInt(8, img.getSize());
				ps.setBinaryStream(9, img.getInputStream(), img.getSize());
				ps.setBoolean(10, img.getFleet());
				executeUpdate(ps, 1);
			}

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
		try (PreparedStatement ps = prepare("UPDATE GALLERY SET NAME=?, DESCRIPTION=?, FLEET=? WHERE (ID=?)")) {
			ps.setString(1, img.getName());
			ps.setString(2, img.getDescription());
			ps.setBoolean(3, img.getFleet());
			ps.setInt(4, img.getID());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Image Like to the database.
	 * @param userID the database ID of the Person liking the Image
	 * @param imgID the image database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void like(int userID, int imgID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO GALLERYSCORE (IMG_ID, PILOT_ID) VALUES (?, ?)")) {
			ps.setInt(1, imgID);
			ps.setInt(2, userID);
			executeUpdate(ps, 1);
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
			try (PreparedStatement ps = prepare("DELETE FROM GALLERY WHERE (ID=?)")) {
				ps.setInt(1, id);
				executeUpdate(ps, 0);
			}

			// Update any Water Cooler threads that link to this.
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.COOLER_THREADS SET IMAGE_ID=0 WHERE (IMAGE_ID=?)")) {
				ps.setInt(1, id);
				executeUpdate(ps, 0);
			}

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
		try (PreparedStatement ps = prepare("DELETE FROM GALLERYSCORE WHERE (IMG_ID=?) AND (PILOT_ID=?)")) {
			ps.setInt(1, imgID);
			ps.setInt(2, pilotID);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}