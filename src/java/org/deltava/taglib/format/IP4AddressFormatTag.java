// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.net.InetAddress;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.NetworkUtils;

/**
 * A JSP tag to format compressed IP addresses.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IP4AddressFormatTag extends TagSupport {

	private byte[] _addr;
	
	/**
	 * Sets the packed 32-bit address to format.
	 * @param addr the packed address
	 */
	public void setAddress(int addr) {
		_addr = NetworkUtils.convertIP(addr);
	}
	
	/**
	 * Displays the formatted address.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
        try {
        	InetAddress ip = InetAddress.getByAddress(_addr);
        	pageContext.getOut().print(ip.getHostAddress());
        } catch (Exception e) {
        	throw new JspException(e);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
	}
}