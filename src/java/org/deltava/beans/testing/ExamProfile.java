// Copyright 2005, 2006, 2007, 2008, 2011, 2012, 2016, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.cache.Cacheable;

/**
 * A class to store Examination profile information.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class ExamProfile implements Comparable<ExamProfile>, PassStatistics, Auditable, Cacheable, ViewEntry {

	private String _name;
	private int _stage;
	private int _questions;
	private int _passScore;
	private int _time;
	private String _eqType;
	private int _minStage;

	private boolean _active;
	private boolean _flightAcademy;
	private boolean _notify;
	
	private final TestStatistics _stats = new TestStatistics();
	private int _poolSize;

	private AirlineInformation _owner;
	private final Collection<AirlineInformation> _airlines = new HashSet<AirlineInformation>();
	private final Collection<Integer> _graderIDs = new HashSet<Integer>();

	/**
	 * Creates a new Examination profile.
	 * @param name the name of the examination
	 * @throws NullPointerException if name is null
	 * @see ExamProfile#getName()
	 */
	public ExamProfile(String name) {
		super();
		setName(name);
	}

	/**
	 * Returns the Examination Name.
	 * @return the exam name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the equipment program required to take this Examination.
	 * @return the equipment program name
	 * @see ExamProfile#setEquipmentType(String)
	 */
	public String getEquipmentType() {
		return _eqType;
	}

	/**
	 * Returns the stage for this Examination.
	 * @return the stage
	 * @see ExamProfile#setStage(int)
	 */
	public int getStage() {
		return _stage;
	}

	/**
	 * Returns the minimum stage required in order to take this Examination.
	 * @return the minimum stage
	 * @see ExamProfile#setMinStage(int)
	 */
	public int getMinStage() {
		return _minStage;
	}

	/**
	 * Returns the number of questions in this Examination.
	 * @return the number of questions
	 * @see ExamProfile#setSize(int)
	 */
	public int getSize() {
		return _questions;
	}

	/**
	 * Returns the minimum percentage required to pass this Examination.
	 * @return the minimum percentage multiplied by 100
	 * @see ExamProfile#setPassScore(int)
	 */
	public int getPassScore() {
		return _passScore;
	}

	/**
	 * Returns the time allowed to complete this Examination.
	 * @return the time in minutes
	 * @see ExamProfile#setTime(int)
	 */
	public int getTime() {
		return _time;
	}

	/**
	 * Returns whether this Examination is avialable to be taken.
	 * @return TRUE if the Examination is active, otherwise FALSE
	 * @see ExamProfile#setActive(boolean)
	 */
	public boolean getActive() {
		return _active;
	}

	/**
	 * Returns whether this Examination is part of the Flight Academy.
	 * @return TRUE if the Examination is part of the Academy, otherwise FALSE
	 * @see ExamProfile#setAcademy(boolean)
	 */
	public boolean getAcademy() {
		return _flightAcademy;
	}

	/**
	 * Returns whether a notification message should be sent on submission.
	 * @return TRUE if a message should be sent, otherwise FALSE
	 * @see ExamProfile#setNotify(boolean)
	 */
	public boolean getNotify() {
		return _notify;
	}

	@Override
	public int getTotal() {
		return _stats.getTotal();
	}

	@Override
	public int getPassCount() {
		return _stats.getPassCount();
	}
	
	/**
	 * Returns the number of questions in this Examination's question pool.
	 * @return the number of questions
	 * @see ExamProfile#setQuestionPoolSize(int)
	 */
	public int getQuestionPoolSize() {
		return _poolSize;
	}

	/**
	 * Returns the Owner Airline for this Examination.
	 * @return an AirlineInformation bean
	 * @see ExamProfile#setOwner(AirlineInformation)
	 */
	public AirlineInformation getOwner() {
		return _owner;
	}

	/**
	 * Returns the Airlines that can access this Examination.
	 * @return a Collection of AirlineInformation beans
	 * @see ExamProfile#addAirline(AirlineInformation)
	 * @see ExamProfile#setAirlines(Collection)
	 */
	public Collection<AirlineInformation> getAirlines() {
		return _airlines;
	}

	/**
	 * Returns the database IDs of all users who can grade this Examination.
	 * @return a Collection of database IDs
	 * @see ExamProfile#addScorerID(int)
	 */
	public Collection<Integer> getScorerIDs() {
		return _graderIDs;
	}

	/**
	 * Sets the stage for this Examination.
	 * @param stage the stage number
	 * @see ExamProfile#getStage()
	 */
	public void setStage(int stage) {
		_stage = Math.max(1, stage);
	}

	/**
	 * Updates the Examination name.
	 * @param name the new name
	 * @throws NullPointerException if name is null
	 * @see ExamProfile#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}

	/**
	 * Sets the equipment program required to take this Examination.
	 * @param eqType the equipment program name
	 * @see ExamProfile#getEquipmentType()
	 */
	public void setEquipmentType(String eqType) {
		_eqType = eqType;
	}

	/**
	 * Sets the minimum stage required to take this Examination.
	 * @param stage the stage number
	 * @see ExamProfile#getMinStage()
	 */
	public void setMinStage(int stage) {
		_minStage = Math.max(1, stage);
	}

	/**
	 * Sets the passing score for this Examination as a percentage.
	 * @param score the passing score, from 0 to 100
	 * @throws IllegalArgumentException if score is negative or &gt; 100
	 * @see ExamProfile#getPassScore()
	 */
	public void setPassScore(int score) {
		if ((score < 0) || (score > 100))
			throw new IllegalArgumentException("Passing score cannot be < 0% or >100%");

		_passScore = score;
	}

	/**
	 * Sets the number of questions in this Examination.
	 * @param size the number of questions
	 * @see ExamProfile#getSize()
	 */
	public void setSize(int size) {
		_questions = Math.max(0, size);
	}

	/**
	 * Sets the time required to complete this Examination.
	 * @param time the time in minutes
	 * @throws IllegalArgumentException if time is zero or negative
	 * @see ExamProfile#getTime()
	 */
	public void setTime(int time) {
		if (time < 1)
			throw new IllegalArgumentException("Time cannot be zero or negative");

		_time = time;
	}

	/**
	 * Marks this Examination as available to be taken.
	 * @param active TRUE if the Examination is active, otherwise FALSE
	 * @see ExamProfile#getActive()
	 */
	public void setActive(boolean active) {
		_active = active;
	}

	/**
	 * Marks this Examination as part of the Flight Academy.
	 * @param academy TRUE if the Examination is part of the Flight Academy, otherwise FALSE
	 * @see ExamProfile#setAcademy(boolean)
	 */
	public void setAcademy(boolean academy) {
		_flightAcademy = academy;
	}

	/**
	 * Sets a notification on Examination submission.
	 * @param doNotify TRUE if a notification should be sent, otherwise FALSE
	 * @see ExamProfile#getNotify()
	 */
	public void setNotify(boolean doNotify) {
		_notify = doNotify;
	}
	
	/**
	 * Updates the total number of times this Examination has been taken.
	 * @param total the number of times
	 * @see ExamProfile#getTotal()
	 */
	public void setTotal(int total) {
		_stats.setTotal(total);
	}
	
	/**
	 * Updates the total numebr of times this Examination has been passed.
	 * @param cnt the number of passed Examinations
	 * @see ExamProfile#getPassCount()
	 */
	public void setPassCount(int cnt) {
		_stats.setPassCount(cnt);
	}
	
	/**
	 * Updates the number of Exam Questions in this Examination's question pool.
	 * @param cnt the number of questions
	 * @see ExamProfile#getQuestionPoolSize()
	 */
	public void setQuestionPoolSize(int cnt) {
		_poolSize = cnt;
	}

	/**
	 * Sets which airline is the owner of this Examination.
	 * @param ai the AirlineInformation bean for the owner airline
	 * @see ExamProfile#getOwner()
	 */
	public void setOwner(AirlineInformation ai) {
		_owner = ai;
		_airlines.add(ai);
	}

	/**
	 * Makes this Examination visible to an Airline.
	 * @param ai the AirlineInformation bean
	 * @see ExamProfile#setAirlines(Collection)
	 * @see ExamProfile#getAirlines()
	 */
	public void addAirline(AirlineInformation ai) {
		_airlines.add(ai);
	}

	/**
	 * Sets the Airlines that this Examination will be visible to.
	 * @param airlines a Collection of AirlineInformation beans
	 */
	public void setAirlines(Collection<AirlineInformation> airlines) {
		_airlines.clear();
		_airlines.add(_owner);
		if (airlines != null)
			_airlines.addAll(airlines);
	}

	/**
	 * Adds a user who can grade this Examination.
	 * @param id the user's database ID
	 * @see ExamProfile#getScorerIDs()
	 */
	public void addScorerID(int id) {
		if (id > 0)
			_graderIDs.add(Integer.valueOf(id));
	}

	@Override
	public int compareTo(ExamProfile e2) {
		int tmpResult = Integer.compare(_stage, e2._stage);
		return (tmpResult != 0) ? tmpResult : _name.compareTo(e2._name);
	}

	@Override
	public Object cacheKey() {
		return _name;
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}

	@Override
	public String toString() {
		return _name;
	}

	@Override
	public String getRowClassName() {
		return !_active ? "warn" : (_flightAcademy ? "opt2" : null);
	}

	@Override
	public String getAuditID() {
		return _name;
	}
}