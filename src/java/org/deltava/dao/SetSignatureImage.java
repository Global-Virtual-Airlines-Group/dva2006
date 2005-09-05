package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to write Signature Images.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class SetSignatureImage extends DAO {

    /**
     * Initialize the Data Access Object.
     * @param c the JDBC connection to use
     */
    public SetSignatureImage(Connection c) {
        super(c);
    }
    
    /**
     * Writes a Signature Image to the Database. The image data should be contained within the Pilot bean. This
     * DAO will issue an INSERT or UPDATE statement depending on the value of the Pilot.hasSignature property.
     * @param p the Pilot containing the Signature image
     * @throws DAOException if a JDBC error occurs
     * @see Pilot#getHasSignature()
     */
    public void write(Pilot p) throws DAOException {
        
        try {
            // Check if we're doing an UPDATE or an INSERT
            if (p.getHasSignature()) {
                prepareStatementWithoutLimits("UPDATE SIGNATURES SET WC_SIG=? WHERE (ID=?)");
                _ps.setBinaryStream(1, p.getInputStream(), p.getSize());
                _ps.setInt(2, p.getID());
            } else {
                prepareStatementWithoutLimits("INSERT INTO SIGNATURES (ID, WC_SIG) VALUES(?, ?)");
                _ps.setInt(1, p.getID());
                _ps.setBinaryStream(2, p.getInputStream(), p.getSize());
            }
            
            executeUpdate(1);
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Removes a Signature Image from the database.
     * @param pilotID the Pilot ID
     * @throws DAOException if a JDBC error occurs
     */
    public void delete(int pilotID) throws DAOException {
        
        try {
            prepareStatementWithoutLimits("DELETE FROM SIGNATURES WHERE (ID=?)");
            _ps.setInt(1, pilotID);
            executeUpdate(0);
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
}