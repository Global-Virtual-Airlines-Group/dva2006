// Copyright 2008, 2010, 2015, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.time.*;
import java.time.format.*;

import org.deltava.beans.Pilot;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to embed a Water Cooler signature tag.
 * @author Luke
 * @version 10.5
 * @since 2.3
 */

public class SignatureTag extends ImageBeanTag {
	
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
	
	@Override
	public void release() {
		super.release();
		_aCode = null;
		_noCache = false;
		_classes.add("nophone");
	}
	
	@Override
	public int doStartTag() {
		if (!_usr.getHasSignature())
			return SKIP_BODY;

		// Build the source and render
		StringBuilder buf = new StringBuilder("sig/");
		buf.append(_df.format(ZonedDateTime.ofInstant(_usr.getSignatureModified(), ZoneOffset.UTC)));
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
		return SKIP_BODY;
	}
}