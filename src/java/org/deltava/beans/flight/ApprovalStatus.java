// Copyright 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to track Flight Report post-approval operations queue entries. This has a different sorting order from its parent class, where
 * the primary sort key is Pilot ID, followed by creation date/time.
 * @author Luke
 * @version 12.5
 * @since 12.1
 */

public class ApprovalStatus extends DatabaseBean implements AuthoredBean {
	
	private int _authorID;
	private final Instant _createdOn;
	private final Collection<ApprovalOperation> _ops = new HashSet<ApprovalOperation>();

	/**
	 * Creates the bean.
	 * @param id the Flight Report database ID
	 * @param createdOn the date/time this queue entry was created
	 */
	public ApprovalStatus(int id, Instant createdOn) {
		super();
		setID(id);
		_createdOn = createdOn;
	}
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the date/time this queue entry was created.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Adds an incomplete post-approval operation for this Flight.
	 * @param op the ApprovalOperation
	 */
	public void add(ApprovalOperation op) {
		_ops.add(op);
	}
	
	/**
	 * Checks if a post-approval operation is pending for this Flight.
	 * @param op the ApprovalOperation
	 * @return TRUE if the Operation is <i>incomplete</i>, otherwise FALSE
	 */
	public boolean isPending(ApprovalOperation op) {
		return _ops.contains(op);
	}
	
	/**
	 * Removes a complete post-approval operation from this Flight.
	 * @param op the ApprovalOperation
	 */
	public void remove(ApprovalOperation op) {
		_ops.remove(op);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof ApprovalStatus aq) {
			int tmpResult = Integer.compare(_authorID, aq.getAuthorID());
			return (tmpResult == 0) ? _createdOn.compareTo(aq._createdOn) : tmpResult;
		}
		
		return super.compareTo(o) ;
	}
}