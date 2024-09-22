// Copyright 2005, 2010, 2012, 2013, 2016, 2021, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.time.*;
import java.time.format.DateTimeFormatter;

import javax.servlet.jsp.*;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support the display of formatted date/time values.
 * @author Luke
 * @version 11.3
 * @since 7.0
 */

public class DurationFormatTag extends UserSettingsTag {
	
	private final String DEFAULT_T_FMT = SystemData.get("time.time_format");

	private String _timeFormat = DEFAULT_T_FMT;
	private Duration _d;

	private String _className;
	private String _nullData;
	private boolean _isLong;

	@Override
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		if (_user != null)
			_timeFormat = _user.getTimeFormat();
	}

	/**
	 * Updates the CSS class for this formatted date/time. This will automatically enclose the output in a &lt;SPAN&gt; tag.
	 * @param cName the class Name(s)
	 */
	public void setClassName(String cName) {
		_className = cName;
	}

	/**
	 * Updates the text to display if the date is null.
	 * @param defaultText the text to display
	 */
	public void setDefault(String defaultText) {
		_nullData = defaultText;
	}

	/**
	 * Updates whether to use a text format description.
	 * @param isLong TRUE if text description should be used, otherwise FALSE
	 */
	public void setLong(boolean isLong) {
		_isLong = isLong;
	}

	/**
	 * Updates the time format pattern.
	 * @param pattern the pattern string
	 * @throws IllegalArgumentException if {@link DateTimeFormatter} cannot interpret the pattern
	 */
	public void setT(String pattern) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern); // validate pattern
		if (df != null)
			_timeFormat = pattern;
	}

	/**
	 * Sets the duration to display.
	 * @param d the duration
	 */
	public void setDuration(Duration d) {
		_d = d;
	}

	@Override
	public void release() {
		super.release();
		_timeFormat = DEFAULT_T_FMT;
		_className = null;
		_isLong = false;
	}

	/**
	 * Formats the date/time and writes it to the JSP output writer.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			if (_className != null) {
				out.print("<span class=\"");
				out.print(_className);
				out.print("\">");
			}

			// Write the formatted date
			if (_d == null)
				out.print(_nullData);
			else if (!_isLong) {
				long d = _d.toDays();
				if (d > 0) {
					out.print(d);
					out.print(':');
				}
				
				DateTimeFormatter df = DateTimeFormatter.ofPattern(_timeFormat);
				ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(_d.getSeconds()), ZoneId.of("Z"));
				out.print(df.format(zdt));
			} else {
				Duration d2 = Duration.from(_d);
				long d = d2.toDays();
				if (d > 0) {
					d2 = d2.minusDays(d);
					out.print(d);
					out.print(" day");
					if (d > 1) out.print('s');
					if (!d2.isZero()) out.print(' ');
				}
				
				long h = d2.toHours();
				if (h > 0) {
					d2 = d2.minusHours(h);
					out.print(h);
					out.print(" hour");
					if (h > 1) out.print('s');
					if (!d2.isZero()) out.print(' ');
				}
				
				long m = d2.toMinutes();
				if (m > 0) {
					d2 = d2.minusMinutes(m);
					out.print(m);
					out.print(" minute");
					if (m > 1) out.print('s');
					if (!d2.isZero()) out.print(' ');
				}
				
				long s = d2.toSeconds();
				if (s > 0) {
					out.print(s);
					out.print(" second");
					if (s > 1) out.print('s');
				}
			}
			
			if (_className != null)
				out.print("</span>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}