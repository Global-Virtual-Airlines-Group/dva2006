// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An enumeration of Actions that can be placed into a push notification.
 * @author Luke
 * @version 10.2
 * @since 10.0
 */

public enum NotifyActionType implements org.deltava.beans.EnumDescription {
	HOME("Home Page", "home"), PILOTCENTER("Pilot Center", "pilotcenter"), PIREPQUEUE("Flight Queue", "pirepqueue"), PROMOQUEUE("Promotion Queue", "promoqueue"), LOGBOOK("Log Book", "logbook", true), 
	TESTCENTER("Testing Center", "testcenter", true), TXQUEUE("Transfer Queue", "txrequests"), PIREP("Flight Report", "pirep", true), CHECKRIDE("Check Ride", "checkride", true), EXAM("Examination", "exam", true),
	TRANSFER("Program Transfer", "txreqview", true), EVENTCALENDAR("Event Calendar", "eventcalendar"), EVENT("Online Event", "event", true), THREAD("Message Thread", "thread", true), 
	ISSUE("Development Issue", "issue", true), HELP("Help Desk Issue", "hdissue", true), CHREQ("Charter Flight Request", "chreq", true), JOB("Career Opportunity", "job", true);
	
	private final String _url;
	private final String _desc;
	private final boolean _hasID;
	
	/**
	 * Creates an Action Type without an ID.
	 * @param desc the description
	 * @param url the base URL
	 */
	NotifyActionType(String desc, String url) {
		this(desc, url, false);
	}
	
	/**
	 * Creates an Action Type.
	 * @param desc the description
	 * @param url the base URL
	 * @param hasID TRUE if an ID is required, otherwise FALSE
	 */
	NotifyActionType(String desc, String url, boolean hasID) {
		_desc = desc;
		_url = url;
		_hasID = hasID;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns whether this Action Type requires an appended ID.
	 * @return TRUE if an ID is required, otherwise FALSE
	 */
	public boolean hasID() {
		return _hasID;
	}
	
	/**
	 * Returns the base URL for this Action Type.
	 * @return the web site command ID
	 */
	public String getURL() {
		return _url;
	}
}