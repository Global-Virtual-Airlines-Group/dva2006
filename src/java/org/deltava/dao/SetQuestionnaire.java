// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.*;

/**
 * A Data Access Object to write Applicant Questionnaires.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetQuestionnaire extends DAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetQuestionnaire(Connection c) {
      super(c);
   }

   /**
    * Writes an Applicant Questionnaire to the database. This can handle INSERTs and UPDATEs.
    * @param e the Examination bean
    * @throws DAOException if a JDBC error occurs
    * @throws IllegalArgumentException if the Examination name is not &quot;Initial Questionnaire&quot;
    */
   public void write(Examination e) throws DAOException {
      
      // Check the exam name
      if (!Examination.QUESTIONNAIRE_NAME.equals(e.getName()))
         throw new IllegalArgumentException("Invalid Examination - " + e.getName());
      
      // Check if we're adding or updating
      boolean isNew = (e.getID() == 0);
      
      try {
         startTransaction();
         
         // Create the prepared statement
         if (isNew) {
            prepareStatement("INSERT INTO APPEXAMS (APP_ID, STATUS, CREATED_ON, EXPIRY_TIME, SUBMITTED_ON, "
                  + "GRADED_ON, GRADED_BY) VALUES (?, ?, ?, ?, ?, ?, ?)");
         } else {
            prepareStatement("UPDATE APPEXAMS SET APP_ID=?, STATUS=?, CREATED_ON=?, EXPIRY_TIME=?, "
                  + "SUBMITTED_ON=?, GRADED_ON=?, GRADED_BY=? WHERE (ID=?)");
            _ps.setInt(8, e.getID());
         }
         
         // Add the prepared statement arguments
         _ps.setInt(1, e.getPilotID());
         _ps.setInt(2, e.getStatus());
         _ps.setTimestamp(3, createTimestamp(e.getDate()));
         _ps.setTimestamp(4, createTimestamp(e.getExpiryDate()));
         _ps.setTimestamp(5, createTimestamp(e.getSubmittedOn()));
         _ps.setTimestamp(6, createTimestamp(e.getScoredOn()));
         _ps.setInt(7, e.getScorerID());
         
         // Update the database
         executeUpdate(1);
         
         // If we're writing a new exam, get the ID
         if (isNew)
            e.setID(getNewID());
         
         // Prepare the statement for the questions
         if (isNew) {
            prepareStatement("INSERT INTO APPQUESTIONS (QUESTION_ID, QUESTION, CORRECT_ANSWER, "
                  + "ANSWER, CORRECT, EXAM_ID, QUESTION_NO) VALUES (?, ?, ?, ?, ?, ?, ?)");
         } else {
            prepareStatement("UPDATE APPQUESTIONS SET QUESTION_ID=?, QUESTION=?, CORRECT_ANSWER=?, "
                  + "ANSWER=?, CORRECT=? WHERE (EXAM_ID=?) AND (QUESTION_NO=?)");
         }

         // Add the questions
         _ps.setInt(6, e.getID());
         for (Iterator i = e.getQuestions().iterator(); i.hasNext(); ) {
            Question q = (Question) i.next();
            _ps.setInt(1, q.getID());
            _ps.setString(2, q.getQuestion());
            _ps.setString(3, q.getCorrectAnswer());
            _ps.setString(4, q.getAnswer());
            _ps.setBoolean(5, q.isCorrect());
            _ps.setInt(7, q.getNumber());
            _ps.addBatch();
         }
         
         // Write the questions
         _ps.executeBatch();
         
         // Commit the transaction and clean up
         commitTransaction();
         _ps.close();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
   
   /**
    * Deletes an Applicant Questionnaire from the database.
    * @param id the Questionnaire database ID
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(int id) throws DAOException {
      try {
         prepareStatement("DELETE FROM APPEXAMS WHERE (ID=?)");
         _ps.setInt(1, id);
         executeUpdate(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Converts an Applicant Questionnaire into a Pilot Examination (when an Applicant is hired).
    * @param e the Questionnaire bean
    * @param pilotID the new Pilot's database ID
    * @throws DAOException if a JDBC error occurs
    * @throws IllegalArgumentException if the Examination name is not &quot;Initial Questionnaire&quot;
    */
   public void convertToExam(Examination e, int pilotID) throws DAOException {
      
      // Check the exam name
      if (!Examination.QUESTIONNAIRE_NAME.equals(e.getName()))
         throw new IllegalArgumentException("Invalid Examination - " + e.getName());

      try {
         startTransaction();
         
         // Create the Examination prepared statement
         prepareStatement("INSERT INTO EXAMS (NAME, PILOT_ID, STATUS, CREATED_ON, SUBMITTED_ON, GRADED_ON, "
         		+ "GRADED_BY, EXPIRY_TIME, PASS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
         _ps.setString(1, e.getName());
         _ps.setInt(2, pilotID);
         _ps.setInt(3, e.getStatus());
         _ps.setTimestamp(4, createTimestamp(e.getDate()));
         _ps.setTimestamp(5, createTimestamp(e.getSubmittedOn()));
         _ps.setTimestamp(6, createTimestamp(e.getScoredOn()));
         _ps.setInt(7, e.getScorerID());
         _ps.setTimestamp(8, createTimestamp(e.getExpiryDate()));
         _ps.setBoolean(9, true);

         // Write the exam and get the new exam ID
         executeUpdate(1);
         int examID = getNewID();
         
         // Prepare the statement for questions
         prepareStatement("INSERT INTO EXAMQUESTIONS (EXAM_ID, QUESTION_ID, QUESTION_NO, QUESTION, " +
               "CORRECT_ANSWER, ANSWER, CORRECT) VALUES (?, ?, ?, ?, ?, ?, ?)");
         _ps.setInt(1, examID);
         
         // Batch the questions
         for (Iterator i = e.getQuestions().iterator(); i.hasNext(); ) {
            Question q = (Question) i.next();
            _ps.setInt(2, q.getID());
            _ps.setInt(3, q.getNumber());
            _ps.setString(4, q.getQuestion());
            _ps.setString(5, q.getCorrectAnswer());
            _ps.setString(6, q.getAnswer());
            _ps.setBoolean(7, q.isCorrect());
            _ps.addBatch();
         }
         
         // Write the questions and clean up
         _ps.executeBatch();
         _ps.close();
         
         // Commit the transaction
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
}