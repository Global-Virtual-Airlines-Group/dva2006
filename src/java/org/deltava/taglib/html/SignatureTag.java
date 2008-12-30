// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.beans.Pilot;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to embed a Water Cooler signature tag.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class SignatureTag extends ImageTag {
	
	private Pilot _usr;
	private String _db;
	private boolean _noCache;

	/**
	 * Creates a new Image element tag.
	 */
	public SignatureTag() {
		super();
	}

	/**
	 * Sets whether the image should be cache-busted.
	 * @param noCache TRUE if no caching, otherwise FALSE
	 */
	public void setNoCache(boolean noCache) {
		_noCache = noCache; 	
	}

	/**
	 * Sets the user to display the signature for.
	 * @param p the Pilot
	 */
	public void setUser(Pilot p) {
		_usr = p;
	}
	
	/**
	 * Sets the database to pull the signature from.
	 * @param dbName the database name
	 */
	public void setDb(String dbName) {
		_db = dbName;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		_db = null;
		_noCache = false;
		super.release();
	}
	
	/**
     * Generates this image's HTML.
     * @throws JspException if an error occurs
     */
	public int doEndTag() throws JspException {
		if (!_usr.getHasSignature())
			return EVAL_PAGE;

		// Build the source and render
		StringBuilder buf = new StringBuilder("/sig/");
		buf.append((_db == null) ? SystemData.get("airline.db") : _db);
		buf.append('/');
		buf.append(_usr.getHexID());
		buf.append('.');
		buf.append(_usr.getSignatureExtension());
		if (_noCache) {
			buf.append("?noCache=");
			buf.append(System.currentTimeMillis());
		}
			
		_data.setAttribute("src", buf.toString());
		return super.doEndTag();
	}
}