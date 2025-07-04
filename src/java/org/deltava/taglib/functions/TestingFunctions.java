// Copyright 2005, 2006, 2007, 2008, 2011, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.testing.*;

/**
 * A JSP Function Library to define Testing Center-related functions.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class TestingFunctions {
	
	// static class
	private TestingFunctions() {
		super();
	}
	
	/**
	 * Checks if a Test is a CheckRide, not an Examination.
	 * @param t the Test
	 * @return TRUE if t is an instanceof CheckRide, otherwise FALSE
	 */
	public static boolean isCheckRide(Test t) {
		return (t instanceof CheckRide);
	}
	
	/**
	 * Returns whether the question is a multiple-choice question.
	 * @param q the Question or QuestionProfile bean
	 * @return TRUE if the question is multiple-choice, otherwise FALSE
	 * @see MultipleChoice
	 */
	public static boolean isMultiChoice(Question q) {
		return (q instanceof MultipleChoice);
	}
	
	/**
	 * Returns whether the question is a route plotting question.
	 * @param q the Question or QuestionProfile bean
	 * @return TRUE if the question is a route plotting question, otherwise FALSE
	 * @see RoutePlot
	 */
	public static boolean isRoutePlot(Question q) {
		return (q instanceof RoutePlot);
	}
	
	/**
	 * Checks if a question has been answered correctly.
	 * @param q the Question
	 * @return TRUE if the answer is correct, otherwise FALSE
	 */
	public static boolean correct(Question q) {
	   return (q != null) && q.isCorrect();
	}
	
	/**
	 * Checks if a question has been answered incorrectly.
	 * @param t the Examination
	 * @param q the Question
	 * @return TRUE if the test is scored and answer is not correct, otherwise FALSE
	 * @throws NullPointerException if t or q are null
	 */
	public static boolean incorrect(Test t, Question q) {
		return ((t.getStatus() == TestStatus.SCORED) && !q.isCorrect());
	}
	
	/**
	 * Checks if a Test has been passed.
	 * @param t the Test
	 * @return TRUE if the test is scored and passed, otherwise FALSE
	 */
	public static boolean isPass(Test t) {
		return (t != null) && (t.getStatus() == TestStatus.SCORED) && t.getPassFail();
	}
	
	/**
	 * Checks if a Test has been failed.
	 * @param t the Test
	 * @return TRUE if the test is scored and not passed, otherwise FALSE
	 */
	public static boolean isFail(Test t) {
		return (t != null) && (t.getStatus() == TestStatus.SCORED) && !t.getPassFail();
	}
	
	/**
	 * Checks if a Test is pending (new or submitted).
	 * @param t the Test - can be null 
	 * @return TRUE if the Test is Submitted or New
	 */
	public static boolean isPending(Test t) {
	   return (t != null) && ((t.getStatus() == TestStatus.SUBMITTED) || (t.getStatus() == TestStatus.NEW));
	}
	
	/**
	 * Check if a Test is submitted.
	 * @param t the Test
	 * @return TRUE if the test has been submitted
	 */
	public static boolean isSubmitted(Test t) {
	   return (t != null) && (t.getStatus() == TestStatus.SUBMITTED);
	}
	
	/**
	 * Returns whether this is a Check Ride waiver or initial hire waiver.
	 * @param cr the CheckRide
	 * @return TRUE if the Check Ride is an iniital hire waiver or check ride waiver, otherwise FALSE
	 */
	public static boolean isWaiver(CheckRide cr) {
		return (cr != null) && ((cr.getType() == RideType.WAIVER) || (cr.getType() == RideType.HIRE));
	}
}