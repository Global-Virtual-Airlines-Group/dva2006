// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to track post-approval operations for a Flight Report.
 * @author Luke
 * @version 12.1
 * @since 12.1
 */

public class ApprovalStatus extends DatabaseBean {
	
	private final Collection<ApprovalOperation> _ops = new HashSet<ApprovalOperation>();

	/**
	 * Creates the bean.
	 * @param id the Flight Report database ID
	 */
	public ApprovalStatus(int id) {
		super();
		setID(id);
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
}