// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * A class to store Push Notification action data.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class NotifyAction {

	private final NotifyActionType _type;
	private final String _url;
	
	private NotifyAction(NotifyActionType nt, String url) {
		super();
		_type = nt;
		_url = url;
	}
	
	/**
	 * Creates a new NotifyAction.
	 * @param nt the NotifyActionType
	 * @param id an optional ID for the URL
	 * @return a NotifyAction
	 */
	public static NotifyAction create(NotifyActionType nt, Object id) {
		StringBuilder urlBuf = new StringBuilder(nt.getURL());
		urlBuf.append(".do");
		if (nt.hasID() && (id != null))
			urlBuf.append("?id=").append(id.toString());
		
		return new NotifyAction(nt, urlBuf.toString());
	}
	
	/**
	 * Returns the action description.
	 * @return the description
	 */
	public String getDescription() {
		return _type.getDescription();
	}
	
	/**
	 * Returns the action URL to open.
	 * @return the URL without any hostname or protocol
	 */
	public String getURL() {
		return _url;
	}
	
	@Override
	public int hashCode() {
		return _url.hashCode();
	}
}