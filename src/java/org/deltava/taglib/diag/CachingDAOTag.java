// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.diag;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.deltava.dao.CachingDAO;

/**
 * A JSP Tag to call static methods on a Caching DAO that cannot be
 * performed via EL in JDK7.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class CachingDAOTag extends SimpleTagSupport {
	
	private CachingDAO _dao;

	private String _hitVar;
	private String _reqVar;
	
	/**
	 * Sets the caching Data Access Object to query.
	 * @param dao the DAO
	 */
	public void setDao(CachingDAO dao) {
		_dao = dao;
	}

	/**
	 * Sets the request attribute to store the hit count in.
	 * @param varName the request attribute name
	 */
	public void setHits(String varName) {
		_hitVar = varName;
	}
	
	/**
	 * Sets the request attribute to store the request count in.
	 * @param varName the request attribute name
	 */
	public void setRequests(String varName) {
		_reqVar = varName;
	}

	/**
	 * Saves the cache hit/request counts in the request  
	 */
	public void doTag() throws JspException {
		try {
			JspContext ctx = getJspContext();
			if (_hitVar != null)
				ctx.setAttribute(_hitVar, new Integer(_dao.getHits()), PageContext.REQUEST_SCOPE);
			if (_reqVar != null)
				ctx.setAttribute(_reqVar, new Integer(_dao.getRequests()), PageContext.REQUEST_SCOPE);
		} catch (Exception e) {
			throw new JspException(e);
		}
	}
}