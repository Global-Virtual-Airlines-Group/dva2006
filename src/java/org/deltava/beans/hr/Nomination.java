// Copyright 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to track Senior Captain nominations. The first nomination is tracked in the Nomination and
 * {@link NominationComment} beans, with further nominations resulting in another NominationComment.
 * @author Luke
 * @version 3.6
 * @since 3.3
 */

public class Nomination extends DatabaseBean implements ViewEntry {
	
	public enum Status {
		PENDING, APPROVED, REJECTED;
	}

	private Instant _created;
	private Quarter _q;
	private Status _status = Status.PENDING;
	
	private int _score;
	private int _commentCount;
	
	private final Collection<NominationComment> _comments = new TreeSet<NominationComment>();
	
	/**
	 * Creates the bean.
	 * @param id the nominated Pilot's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public Nomination(int id) {
		super();
		if (id > 0)
			setID(id);
	}

	/**
	 * Returns the date of the nomination.
	 * @return the nomination date/time
	 */
	public Instant getCreatedOn() {
		return _created;
	}
	
	/**
	 * Returns the status of the nomination.
	 * @return the status code
	 */
	public Status getStatus() {
		return _status;
	}
	
	/**
	 * Returns this Nomination's score.
	 * @return the score
	 * @see Nomination#setScore(int)
	 */
	public int getScore() {
		return _score;
	}
	
	/**
	 * Returns the calendar quarter for this Nomination, which may be different than
	 * the date it was created.
	 * @return the Qurater
	 * @see Nomination#setQuarter(Quarter)
	 */
	public Quarter getQuarter() {
		return _q;
	}
	
	/**
	 * Returns the number of comments on this nomination.
	 * @return the number of comments
	 * @see Nomination#addComment(NominationComment)
	 * @see Nomination#setCommentCount(int)
	 */
	public int getCommentCount() {
		return _comments.isEmpty() ? _commentCount : _comments.size();
	}
	
	/**
	 * Returns all comments on this Nomination.
	 * @return a Collection of NominationComments
	 */
	public Collection<NominationComment> getComments() {
		return new ArrayList<NominationComment>(_comments);
	}
	
	/**
	 * Returns whether a particular user has created a comment.
	 * @param authorID the author's database ID
	 * @return TRUE if the author has created a comment, otherwise FALSE
	 */
	public boolean hasComment(int authorID) {
		for (NominationComment nc : _comments) {
			if (nc.getAuthorID() == authorID)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Adds a comment to this Nomination.
	 * @param nc a NominationComment
	 */
	public void addComment(NominationComment nc) {
		_comments.add(nc);
		if ((_created == null) || nc.getCreatedOn().isBefore(_created))
			_created = nc.getCreatedOn();
	}
	
	/**
	 * Updates the nomination status.
	 * @param st the Status
	 */
	public void setStatus(Status st) {
		_status = st;
	}
	
	/**
	 * Updates the calendar quarter of this Nomination.
	 * @param q the Quarter
	 * @see Nomination#getQuarter()
	 */
	public void setQuarter(Quarter q) {
		_q = q;
	}
	
	/**
	 * Updates the date this nomination was created.
	 * @param dt the date/time the nomination was created
	 * @throws IllegalStateException if comments have been added
	 * @see Nomination#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
		if (!_comments.isEmpty())
			throw new IllegalStateException("Comments already populated");
		
		_created = dt;
	}
	
	/**
	 * Updates the number of comments on this Nomination
	 * @param comments the number of comments
	 * @throws IllegalStateException if comments have been added
	 * @see Nomination#getCommentCount()
	 * @see Nomination#getComments()
	 */
	public void setCommentCount(int comments) {
		if (!_comments.isEmpty())
			throw new IllegalStateException("Comments already populated");
		
		_commentCount = Math.max(1, comments);
	}
	
	/**
	 * Updates this Nomination's score.
	 * @param score the score
	 * @see Nomination#getScore()
	 */
	public void setScore(int score) {
		_score = Math.max(0, score);
	}

	@Override
	public String toString() {
		return "Nomination-" + getID();
	}
	
	/**
	 * Returns the CSS class for this item if rendered in a view table.
	 */
	@Override
	public String getRowClassName() {
		if (_status == Status.PENDING)
			return _q.equals(new Quarter(_created)) ? null : "opt1";
		
		return (_status == Status.REJECTED) ? "error" : "opt2";
	}
}