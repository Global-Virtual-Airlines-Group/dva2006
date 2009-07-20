// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.redirect;

import java.util.*;

/**
 * A bean to store Servlet Request state when redirecting a command. This allows Web Site Commands to save data in the
 * servlet request for display on a result JSP, while at the same time using a redirect so that refreshing the result
 * URL does not invoke the command a second time.
 * @author Luke
 * @version 2.6
 * @since 1.0
 * @see org.deltava.commands.RedirectCommand
 */

class RequestContent implements java.io.Serializable {

	private String _url;
	private Map<String, Object> _attrs = new HashMap<String, Object>();

	/**
	 * Generates a new bean from the current servlet request, and stores the URL to redirect to.
	 * @param url the URL to forward to
	 * @see RequestContent#getURL()
	 */
	RequestContent(String url) {
		super();
		_url = url;
	}

	/**
	 * Returns the saved servlet request attribute names.
	 * @return a Set of attribute names
	 */
	public Set<String> getAttributeNames() {
		return _attrs.keySet();
	}

	/**
	 * Returns the saved servlet request attribute values.
	 * @return a Collection of objects
	 */
	public Collection<Object> getAttributeValues() {
		return _attrs.values();
	}

	/**
	 * Returns a saved servlet request attribute.
	 * @param attrName the attribute name
	 * @return the attribute value, or null if not found
	 */
	public Object getAttribute(String attrName) {
		return _attrs.get(attrName);
	}

	/**
	 * Returns the URL to redirect to. The {@link org.deltava.commands.RedirectCommand} will forward to this URL once it
	 * has restored the servlet request attributes.
	 * @return the URL to forward to
	 */
	public String getURL() {
		return _url;
	}

	/**
	 * Adds a saved servlet request attribute.
	 * @param attrName the attribut name
	 * @param value the attribute value
	 */
	public void setAttribute(String attrName, Object value) {
		_attrs.put(attrName, value);
	}

	/**
	 * Returns the number of saved servlet request attributes.
	 * @return the number of saved attributes
	 */
	public int size() {
		return _attrs.size();
	}
}