// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.text.DecimalFormat;

import javax.servlet.jsp.*;

import org.deltava.beans.econ.*;

/**
 * A JSP tag to display Elite level progress bars.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteProgressTag extends UserSettingsTag {
	
	private static final String DEFAULT_PATTERN = "#,###,###,##0";
	private final DecimalFormat _nF = new DecimalFormat(DEFAULT_PATTERN);
	
	private enum WidthType {
		FIXED, PROPORTIONAL, LOG
	}

	private EliteLevel _lvl;
	private EliteLevel _prev;
	
	private EliteUnit _units = EliteUnit.LEGS;
	private WidthType _wt = WidthType.FIXED;
	private boolean _showUnits;
	
	private int _progress;
	
	private String _className;
	private String _rmClassName;
	private int _width;
	private boolean _isPct;

	/**
	 * Sets the Elite level progress.
	 * @param p the progress in units
	 */
	public void setProgress(int p) {
		_progress = Math.max(0, p);
	}
	
	/**
	 * Sets the progress bar width base.
	 * @param w the base width
	 */
	public void setWidth(int w) {
		_width = w;
	}
	
	/**
	 * Sets the total bar width type.
	 * @param wt P for proportional, F for fixed or L for log5
	 */
	public void setWidthType(String wt) {
		String wt2 = String.valueOf(Character.toUpperCase(wt.charAt(0)));
		for (WidthType wtp : WidthType.values()) {
			if (wtp.name().startsWith(wt2)) {
				_wt = wtp;
				break;
			}
		}
	}
	
	/**
	 * Sets whether the width of the progress bar is in percent.
	 * @param isPct TRUE if percent, FALSE if pixels
	 */
	public void setPercent(boolean isPct) {
		_isPct = isPct;
	}
	
	/**
	 * Sets whether to display units in the progress bar text.
	 * @param showUnits TRUE if units displayed, otherwise FALSE
	 */
	public void setShowUnits(boolean showUnits) {
		_showUnits = showUnits;
	}

	/**
	 * Sets the units to display.
	 * @param u an EliteUnit enumeration
	 */
	public void setUnits(String u) {
		String u2 = String.valueOf(Character.toUpperCase(u.charAt(0)));
		for (EliteUnit unit : EliteUnit.values()) {
			if (unit.name().startsWith(u2)) {
				_units = unit;
				break;
			}
		}
	}

	/**
	 * Sets the Elite level progress to render.
	 * @param lvl the EliteLevel
	 */
	public void setLevel(EliteLevel lvl) {
		_lvl = lvl;
	}
	
	/**
	 * Sets the previous Elite level.
	 * @param lvl the previous EliteLevel
	 */
	public void setPrev(EliteLevel lvl) {
		_prev = lvl;
	}
	
	/**
	 * Sets the completed progress bar CSS class name.
	 * @param cn the class name
	 */
	public void setClassName(String cn) {
		_className = cn;
	}
	
	/**
	 * Sets the remaining progress bar CSS class name.
	 * @param cn the class name
	 */
	public void setRemainingClassName(String cn) {
		_rmClassName = cn;
	}
	
    @Override
	public void setPageContext(PageContext ctxt) {
        super.setPageContext(ctxt);
        if (_user != null) {
        	String pattern = _user.getNumberFormat();
            if (pattern.indexOf('.') != -1)
                _nF.applyPattern(pattern.substring(0, pattern.indexOf('.')));
            else
            	_nF.applyPattern(pattern);
        }
    }
    
	@Override
	public int doEndTag() throws JspException {
		if (_lvl == null) return EVAL_PAGE;
		if (_prev == null) _prev = EliteLevel.EMPTY;
		
		// Calculate bar width
		int tg = 0; int total = 0;
		switch (_units) {
			case DISTANCE:
				total = _lvl.getDistance();
				tg = total - _prev.getDistance();
				break;
				
			case POINTS :
				total = _lvl.getPoints();
				tg = total - _prev.getPoints();
				break;
				
			default:
				total = _lvl.getLegs();
				tg = total - _prev.getLegs();
		}
		
		int remaining = Math.max(0, tg - _progress);
		float pctComplete = _progress * 1.0f / tg;
		if ((pctComplete > 0f) && (pctComplete < 1.0f))
			pctComplete = Math.max(0.075f, Math.min(0.925f, pctComplete)); // Ensure the actual bar widths are at leasst 7.5%
		
		// Calculate total bar width
		int totalWidth = switch (_wt) {
			case PROPORTIONAL -> (tg * _width);
			case LOG -> (int) Math.round(Math.log(tg) / Math.log(5) * _width);
			default -> _width;
		};

    	JspWriter out = pageContext.getOut();
    	try {
    		out.print("<span title=\"");
			out.print(_lvl.getName());
			out.print(" (");
			out.print(_nF.format(_progress));
			out.print(" / ");
			out.print(_nF.format(tg));
			out.print(") - ");
			out.print(_nF.format(total));
			out.print(" total\">");
    		
    		if (_progress > 0) {
    			out.print("<span ");
    			if (_className != null) {
    				out.print("class=\"");
    				out.print(_className);
    				out.print("\" ");
    			}
    			
    			out.print("style=\"width:");
    			out.print(Math.round(pctComplete * totalWidth));
    			if (_isPct)
    				out.print('%');
    		
    			out.print("; background-color:#");
    			out.print(_lvl.getHexColor());
    			out.print(";\">");
    			out.print(_nF.format(_progress));
    			if (_showUnits && (pctComplete > .225f)) {
    				out.print("<span class=\"nophone\"> ");
   					out.print(_units.getDescription());
       				if (_progress != 1) out.print('s');	
    				out.print("</span>");
    			}
    			
    			out.print("</span>");
    		}
    		
    		if (remaining > 0) {
    			out.print("<span ");
    			if (_rmClassName != null) {
    				out.print("class=\"");
    				out.print(_rmClassName);
    				out.print("\" ");
    			}
    			
    			out.print("style=\"width:");
    			out.print(Math.round((1f-pctComplete) * totalWidth));
    			if (_isPct)
    				out.print('%');
    			
    			out.print("; background-color:#");
    			out.print(_lvl.getHexColor());
    			out.print(";\">");
    			out.print(_nF.format(remaining));
    			if (_showUnits && (pctComplete < .775f)) {
    				out.print("<span class=\"nophone\"> ");
   					out.print(_units.getDescription());
   					if (remaining != 1) out.print('s');
    				out.print("</span>");
    			}
    			
    			out.print("</span>");
    		}
    		
    		out.print("</span>");
    		return EVAL_PAGE;
    	} catch (Exception e) {
    		throw new JspException(e);
    	} finally {
    		release();
    	}
	}
	
	@Override
	public void release() {
		super.release();
		_prev = null;
		_units = EliteUnit.LEGS;
		_showUnits = false;
		_wt = WidthType.FIXED;
		_rmClassName = null;
		_isPct = false;
		_nF.applyPattern(DEFAULT_PATTERN);
		_nF.setParseIntegerOnly(true);
	}
}