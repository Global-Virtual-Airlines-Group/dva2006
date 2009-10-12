// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.diag;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.deltava.dao.CachingDAO;

/**
 * A JSP Tag to call static methods on a Caching DAO that cannot be
 * performed via EL in JDK7.
 * @author Luke
 * @version 2.6
 * @since 2.4
 */

public class CachingDAOTag extends SimpleTagSupport {
	
	private CachingDAO _dao;
	private String _infoVar;
	
	/**
	 * Sets the caching Data Access Object to query.
	 * @param dao the DAO
	 */
	public void setDao(CachingDAO dao) {
		_dao = dao;
	}

	/**
	 * Sets the page attribute to store the cache info in.
	 * @param varName the page attribute name
	 */
	public void setVar(String varName) {
		_infoVar = varName;
	}
	
	/**
	 * Saves the cache hit/request counts in the request  
	 */
	public void doTag() throws JspException {
		try {
			JspContext ctx = getJspContext();
			ctx.setAttribute(_infoVar, _dao.getCacheInfo(), PageContext.PAGE_SCOPE);
		} catch (Exception e) {
			throw new JspException(e);
		}
	}
}