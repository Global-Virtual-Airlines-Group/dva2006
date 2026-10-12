// Copyright 2023, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.text.DecimalFormatSymbols;

import jakarta.servlet.jsp.*;

import org.deltava.beans.flight.LandingRating;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to format landing scores.
 * @author Luke
 * @version 12.5
 * @since 10.4
 */

public class LandingScoreFormatTag extends NumberFormatTag {
	
	private static final String DEFAULT_PATTERN = "#0.00";
	private static final int DEFAULT_MAX = 100;
	
	private LandingRating _rating;
	private int _maxValue = DEFAULT_MAX;
	private String _default;
	
	/**
     * Initializes the tag and the number formatter.
     */
	public LandingScoreFormatTag() {
		super(DEFAULT_PATTERN);
	}
	
	/**
	 * Updates the default to display if no score has been calculated. 
	 * @param s the default string, or null to suppress display
	 */
	public void setDefault(String s) {
		_default = s;
	}
	
	/**
	 * Sets the maximum value for this score. Ratings will be normalized from 1 to 100.
	 * @param max the maximum value
	 */
	public void setMax(int max) {
		_maxValue = max;
	}
	
    @Override
	public void setPageContext(PageContext ctxt) {
        super.setPageContext(ctxt);
        if (_user != null) {
        	DecimalFormatSymbols sym = _nF.getDecimalFormatSymbols();
        	_nF.applyPattern(DEFAULT_PATTERN);
        	_nF.setDecimalFormatSymbols(sym);
        }
    }
	
    @Override
    public int doStartTag() {
    	if (_value == null) _value = Double.valueOf(-1);
    	if (_maxValue != DEFAULT_MAX)
    		_value = Double.valueOf(_value.doubleValue() * 100 / _maxValue); // normalize
    		
    	_rating = LandingRating.rate(_value.doubleValue());
    	setStyle(String.format("color:#%s", _rating.getHexColor()));
    	return SKIP_BODY;
    }
	
	@Override
	public int doEndTag() throws JspException {
		if ((_rating == LandingRating.UNKNOWN) && StringUtils.isEmpty(_default))
			return EVAL_PAGE;
		
		try {
			openSpan();
			pageContext.getOut().print((_rating == LandingRating.UNKNOWN) ? _default : _nF.format(_value.doubleValue()));
			closeSpan();
			return EVAL_PAGE;
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
	}

	@Override
	public void release() {
		_default = null;
		_maxValue = DEFAULT_MAX;		
		super.release(DEFAULT_PATTERN);
	}
}