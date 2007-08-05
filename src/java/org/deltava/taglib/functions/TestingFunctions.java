// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.testing.*;

/**
 * A JSP Function Library to define Testing Center-related functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TestingFunctions {
	
	/**
	 * Checks if a Test is a CheckRide, not an Examination.
	 * @param t the Test
	 * @return TRUE if t is an instanceof CheckRide, otherwise FALSE
	 */
	public static boolean isCheckRide(Test t) {
		return (t instanceof CheckRide);
	}
	
	/**
	 * Returns wether the question is a multiple-choice question
	 * @param q the Question or QuestionProfile bean
	 * @return TRUE if the question is multiple-choice, otherwise FALSE
	 */
	public static boolean isMultiChoice(Question q) {
		return (q instanceof MultipleChoice);
	}
	
	/**
	 * Checks if a question has been answered correctly.
	 * @param q the Question
	 * @return TRUE if the answer is correct, otherwise FALSE
	 * @throws NullPointerException if q is null
	 */
	public static boolean correct(Question q) {
	   return q.isCorrect();
	}
	
	/**
	 * Checks if a question has been answered incorrectly.
	 * @param t the Examination
	 * @param q the Question
	 * @return TRUE if the test is scored and answer is not correct, otherwise FALSE
	 * @throws NullPointerException if t or q are null
	 */
	public static boolean incorrect(Test t, Question q) {
		return ((t.getStatus() == Test.SCORED) && !q.isCorrect());
	}
	
	/**
	 * Checks if a Test has been passed.
	 * @param t the Test
	 * @return TRUE if the test is scored and passed, otherwise FALSE
	 * @throws NullPointerException if t is null
	 */
	public static boolean isPass(Test t) {
		return ((t.getStatus() == Test.SCORED) && t.getPassFail());
	}
	
	/**
	 * Checks if a Test has been failed.
	 * @param t the Test
	 * @return TRUE if the test is scored and not passed, otherwise FALSE
	 * @throws NullPointerException if t is null
	 */
	public static boolean isFail(Test t) {
		return ((t.getStatus() == Test.SCORED) && !t.getPassFail());
	}
	
	/**
	 * Checks if a Test is pending (new or submitted).
	 * @param t the Test - can be null 
	 * @return TRUE if the Test is Submitted or New
	 */
	public static boolean isPending(Test t) {
	   return (t != null) && ((t.getStatus() == Test.SUBMITTED) || (t.getStatus() == Test.NEW));
	}
	
	/**
	 * Check if a Test is submitted.
	 * @param t the Test
	 * @return TRUE if the test has been submitted
	 * @throws NullPointerException if t is null
	 */
	public static boolean isSubmitted(Test t) {
	   return (t != null) && (t.getStatus() == Test.SUBMITTED);
	}
}