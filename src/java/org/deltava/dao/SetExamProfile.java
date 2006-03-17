// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
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
               "ACTIVE=?, EQTYPE=?, MIN_STAGE=?, ACADEMY=? WHERE (NAME=?)");
         _ps.setInt(1, ep.getStage());
         _ps.setInt(2, ep.getSize());
         _ps.setInt(3, ep.getPassScore());
         _ps.setInt(4, ep.getTime());
         _ps.setBoolean(5, ep.getActive());
         _ps.setString(6, ep.getEquipmentType());
         _ps.setInt(7, ep.getMinStage());
         _ps.setBoolean(8, ep.getAcademy());
         _ps.setString(9, ep.getName());
         
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
               "EQTYPE, MIN_STAGE, ACADEMY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
         _ps.setString(1, ep.getName());
         _ps.setInt(2, ep.getStage());
         _ps.setInt(3, ep.getSize());
         _ps.setInt(4, ep.getPassScore());
         _ps.setInt(5, ep.getTime());
         _ps.setBoolean(6, ep.getActive());
         _ps.setString(7, ep.getEquipmentType());
         _ps.setInt(8, ep.getMinStage());
         _ps.setBoolean(9, ep.getAcademy());
         
         // Execute the update
         executeUpdate(1);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
	 * Writes an Examination Question Profile to the database. This call can handle both INSERT and UPDATE operations.
	 * If an INSERT operation is performed, the auto-assigned database ID will be set in the bean.
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
            
            // Clear out the multiple choice options if we have them
            if (qp instanceof MultipleChoice) {
            	prepareStatementWithoutLimits("DELETE FROM QUESTIONMINFO WHERE (ID=?)");
            	_ps.setInt(1, qp.getID());
            	executeUpdate(0);
            }
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
         
         // Write the multiple choice entries
         if (qp instanceof MultipleChoice) {
        	 MultipleChoice mq = (MultipleChoice) qp;
        	 prepareStatementWithoutLimits("INSERT INTO QUESTIONMINFO (ID, SEQ, ANSWER) VALUES (?, ?, ?)");
        	 _ps.setInt(1, qp.getID());
        	 
        	 // Write the entries
        	 int seq = 0;
        	 for (Iterator<String> i = mq.getChoices().iterator(); i.hasNext(); ) {
        		 String choice = i.next();
        		 _ps.setInt(2, ++seq);
        		 _ps.setString(3, choice);
        		 _ps.addBatch();
        	 }
        	 
        	 // Execute the batch transaction
        	 _ps.executeBatch();
        	 _ps.close();
         }
         
         // Commit the transaction
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }

   /**
	 * Writes a multiple-choice Examination Question Profile to the database. This call can handle both INSERT and
	 * UPDATE operations. If an INSERT operation is performed, the auto-assigned database ID will be set in the bean.
	 * @param mqp the QuestionProfile bean to write
	 * @throws DAOException if a JDBC error occurs
	 */
   public void write(MultiChoiceQuestionProfile mqp) throws DAOException {
	   
	   boolean isNew = (mqp.getID() == 0);
	   try {
		   startTransaction();
		   write((QuestionProfile) mqp);
		   
		   // If this is not a new profile, then delete the previous choices
		   if (!isNew) {
			   prepareStatementWithoutLimits("DELETE FROM QUESTIONMINFO WHERE (ID=?)");
			   _ps.setInt(1, mqp.getID());
			   executeUpdate(0);
		   }
		   
		   // Write question choices
		   prepareStatementWithoutLimits("INSERT INTO QUESTIONMINFO (ID, SEQ, ANSWER) VALUES (?, ?, ?)");
		   _ps.setInt(1, mqp.getID());
		   int seq = 0;
		   for (Iterator<String> i = mqp.getChoices().iterator(); i.hasNext(); ) {
			   _ps.setInt(2, ++seq);
			   _ps.setString(3, i.next());
			   _ps.addBatch();
		   }
		   
		   // Execute the batch update
		   _ps.executeBatch();
		   _ps.close();
		   
		   // Commit
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
   
   /**
    * Deletes an Examination Question profile from the database.
    * @param qp the QuestionProfile bean
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(QuestionProfile qp) throws DAOException {
	   try {
		   prepareStatement("DELETE FROM QUESTIONINFO WHERE (QUESTION_ID=?)");
		   _ps.setInt(1, qp.getID());
		   executeUpdate(1);
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }

   /**
    * Deletes a Check Ride Script from the database.
    * @param sc the CheckRideScript bean
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(CheckRideScript sc) throws DAOException {
	   try {
		   prepareStatement("DELETE FROM CR_DESCS WHERE (EQTYPE=?) AND (EQPROGRAM=?)");
		   _ps.setString(1, sc.getEquipmentType());
		   _ps.setString(2, sc.getProgram());
		   executeUpdate(1);
	   } catch (SQLException se) {
		   throw new DAOException(se);
	   }
   }
}