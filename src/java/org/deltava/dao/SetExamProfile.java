// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.testing.*;

/**
 * A Data Access Object for writing Examination/Question Profiles and Check Ride scripts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetExamProfile extends DAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetExamProfile(Connection c) {
      super(c);
   }

   /**
    * Saves an existing Examination Profile to the database.
    * @param ep the ExamProfile bean to update
    * @throws DAOException if a JDBC error occurs
    */
   public void update(ExamProfile ep) throws DAOException {
      try {
         prepareStatement("UPDATE EXAMINFO SET STAGE=?, QUESTIONS=?, PASS_SCORE=?, TIME=?, "+
               "ACTIVE=?, REQUIRES_RIDE=?, EQTYPE=? WHERE (NAME=?)");
         _ps.setInt(1, ep.getStage());
         _ps.setInt(2, ep.getSize());
         _ps.setInt(3, ep.getPassScore());
         _ps.setInt(4, ep.getTime());
         _ps.setBoolean(5, ep.getActive());
         _ps.setBoolean(6, ep.getNeedsCheckRide());
         _ps.setString(7, ep.getEquipmentType());
         _ps.setString(8, ep.getName());
         
         // Execute the update
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Saves a new Examination Profile to the database.
    * @param ep the ExamProfile bean to save
    * @throws DAOException if a JDBC error occurs
    */
   public void create(ExamProfile ep) throws DAOException {
      try {
         prepareStatement("INSERT INTO EXAMINFO (NAME, STAGE, QUESTIONS, PASS_SCORE, TIME, ACTIVE, " +
               "REQUIRES_RIDE, EQTYPE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
         _ps.setString(1, ep.getName());
         _ps.setInt(2, ep.getStage());
         _ps.setInt(3, ep.getSize());
         _ps.setInt(4, ep.getPassScore());
         _ps.setInt(5, ep.getTime());
         _ps.setBoolean(6, ep.getActive());
         _ps.setBoolean(7, ep.getNeedsCheckRide());
         _ps.setString(8, ep.getEquipmentType());
         
         // Execute the update
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Writes an Examination Question Profile to the database. This call can handle both INSERT and UPDATE
    * operations. If an INSERT operation is performed, the auto-assigned database ID will be set in the bean.
    * @param qp the QuestionProfile bean to write
    * @throws DAOException if a JDBC error occurs
    */
   public void write(QuestionProfile qp) throws DAOException {
      try {
         startTransaction();
         
         // Prepare different statements for INSERT and UPDATE operations
         if (qp.getID() == 0) {
            prepareStatement("INSERT INTO QUESTIONINFO (QUESTION, CORRECT, ACTIVE) VALUES (?, ?, ?)");
         } else {
            prepareStatement("UPDATE QUESTIONINFO SET QUESTION=?, CORRECT=?, ACTIVE=? WHERE (ID=?)");
            _ps.setInt(4, qp.getID());
         }
         
         // Set prepared statement and write the question
         _ps.setString(1, qp.getQuestion());
         _ps.setString(2, qp.getCorrectAnswer());
         _ps.setBoolean(3, qp.getActive());
         executeUpdate(1);
         
         // If this is a new question profile, get the ID back from the database, otherwise clear the exam names
         if (qp.getID() == 0) {
            qp.setID(getNewID());
         } else {
            prepareStatementWithoutLimits("DELETE FROM QE_INFO WHERE (QUESTION_ID=?)");
            _ps.setInt(1, qp.getID());
            executeUpdate(0);
         }
         
         // Write the exam names
         prepareStatementWithoutLimits("INSERT INTO QE_INFO (QUESTION_ID, EXAM_NAME) VALUES (?, ?)");
         _ps.setInt(1, qp.getID());
         for (Iterator i = qp.getExamNames().iterator(); i.hasNext(); ) {
            String examName = (String) i.next();
            _ps.setString(2, examName);
            _ps.addBatch();
         }
         
         // Execute the batch statement and clean up
         _ps.executeBatch();
         _ps.close();
         
         // Commit the transaction
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
   
   /**
    * Writes a Check Ride script to the database. This call can handle both INSERT and UPDATE operations.
    * @param sc the Check Ride script
    * @throws DAOException if a JDBC error occurs
    */
   public void write(CheckRideScript sc) throws DAOException {
      try {
         prepareStatement("REPLACE INTO CR_DESCS (EQTYPE, EQPROGRAM, BODY) VALUES (?, ?, ?)");
         _ps.setString(1, sc.getEquipmentType());
         _ps.setString(2, sc.getProgram());
         _ps.setString(3, sc.getDescription());
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}