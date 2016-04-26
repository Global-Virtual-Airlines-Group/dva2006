// Copyright 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store e-mail notification types.
 * @author Luke
 * @version 7.0
 * @since 3.6
 */

public enum Notification implements ComboAlias {
	
	NEWS("News"), EVENT("Online Event"), FLEET("Library"), PIREP("Flight Report"), JOB("Job Posting");

	private String _label;
	
	Notification(String label) {
		_label = label;
	}
	
	@Override
	public String getComboName() {
		return getLabel();
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
	
	/**
	 * Returns the notification code.
	 * @return the code
	 */
	public int getCode() {
		return 1 << ordinal();
	}
	
	public String getLabel() {
		StringBuilder buf = new StringBuilder("Send ");
		buf.append(_label);
		buf.append(" Notifications");
		return buf.toString();
	}
}