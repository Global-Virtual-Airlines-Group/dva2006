// Copyright 2008, 2010, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.time.Instant;
import java.time.format.*;

import javax.servlet.jsp.JspException;

import org.deltava.beans.Pilot;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to embed a Water Cooler signature tag.
 * @author Luke
 * @version 7.0
 * @since 2.3
 */

public class SignatureTag extends ImageTag {
	
	private final DateTimeFormatter _df = new DateTimeFormatterBuilder().appendPattern("yyyyMMddHHmm").toFormatter();
	
	private Pilot _usr;
	private String _aCode;
	private boolean _noCache;

	/**
	 * Creates a new Image element tag.
	 */
	public SignatureTag() {
		super();
		_classes.add("nophone");
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
	 * Sets the Airline to pull the signature from.
	 * @param c the Airline code
	 */
	public void setCode(String c) {
		_aCode = c;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_aCode = null;
		_noCache = false;
		_classes.add("nophone");
	}
	
	/**
     * Generates this image's HTML.
     * @throws JspException if an error occurs
     */
	@Override
	public int doEndTag() throws JspException {
		if (!_usr.getHasSignature())
			return EVAL_PAGE;

		// Build the source and render
		StringBuilder buf = new StringBuilder("/sig/");
		buf.append(_df.format(_usr.getSignatureModified()));
		buf.append('/');
		buf.append((_aCode == null) ? SystemData.get("airline.code") : _aCode);
		buf.append('/');
		buf.append(_usr.getHexID());
		buf.append('.');
		buf.append(_usr.getSignatureExtension());
		if (_noCache) {
			buf.append("?noCache=");
			buf.append(Instant.now().getEpochSecond());
		}
			
		_data.setAttribute("src", buf.toString());
		return super.doEndTag();
	}
}