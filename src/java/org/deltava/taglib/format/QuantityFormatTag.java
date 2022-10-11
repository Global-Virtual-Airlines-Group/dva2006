// Copyright 2007, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;

/**
 * A JSP Tag to display quantities of a particular item.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public class QuantityFormatTag extends IntegerFormatTag {
	
	private String _singleLabel;
	private String _pluralLabel;
	
	/**
	 * Updates the label used for a single value.
	 * @param label the single value label
	 */
	public void setSingle(String label) {
		_singleLabel = label;
	}
	
	/**
	 * Updates the label used for plural values. If none is specified, the label used for single values
	 * will be used.
	 * @param label the plural value label
	 */
	public void setPlural(String label) {
		_pluralLabel = label;
	}
	
	/**
	 * Updates the plural tag to the singular tag if not set.
	 * @return SKIP_BODY always
	 * @throws JspException never
	 */
	@Override
	public int doStartTag() throws JspException {
		if (_pluralLabel == null)
			_pluralLabel = _singleLabel + "s";
		if (_zeroValue == null)
			_zeroValue = _pluralLabel;
		
		return SKIP_BODY;
	}

	@Override
	public void release() {
		super.release();
		_pluralLabel = null;
	}
	
    /**
     * Formats the number and quantity and writes them to the JSP output writer. If the value cannot be parsed, it is output &quot;as is&quot;.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     */
	@Override
	public int doEndTag() throws JspException {
		
		// Determine the quantity label
		String label = _pluralLabel;
		if (_value.longValue() == 1)
			label = _singleLabel;
		
		// Render the quantity
		super.doEndTag();
		try {
			if (_value.longValue() != 0) {
				JspWriter out = pageContext.getOut();
				out.print(' ');
				out.print(label);
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
        
        return EVAL_PAGE;
	}
}