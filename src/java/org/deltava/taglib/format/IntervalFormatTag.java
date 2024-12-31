// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.List;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A JSP tag to display very short time intervals.
 * @author Luke
 * @version 11.4
 * @since 11.4
 * @see DurationFormatTag
 */

public class IntervalFormatTag extends DecimalFormatTag {
	
	private static final List<String> ABBRS = List.of("ns", "us", "ms", "s", "m", "h", "d");
	
	private Duration _d;
	private TimeUnit _u = TimeUnit.SECONDS;
	
	/**
	 * Sets the duration to render.
	 * @param d the Duration
	 */
	public void setDuration(Duration d) {
		_d = d;
	}
	
	/**
	 * Sets the time unit to display the interval with.
	 * @param u the unit abbreviation
	 */
	public void setUnit(String u) {
		if (u == null) return;
		int idx = ABBRS.indexOf(u.toLowerCase());
		if (idx != -1)
			_u = TimeUnit.values()[idx];
	}
	
	@Override
	public void release() {
		super.release();
		_u = TimeUnit.SECONDS;
	}
	
	/**
     * Appends the unit abbreviation to the value.
     */
    @Override
    protected void printValue() throws Exception {
    	super.printValue();
    	pageContext.getOut().print(ABBRS.get(_u.ordinal()));
    }
	
    /**
     * Checks that a non-null interval duration has been provided, and performs unit conversion.
     * @return SKIP_BODY always
     */
	@Override
	public int doStartTag() {
		if (_d == null) _d = Duration.ZERO;
		long v = _u.convert(_d); long av = Math.abs(v);
		if ((av < 100) && (_u != TimeUnit.NANOSECONDS)) { // If we get a low number, move to the next unit down
			TimeUnit u2 = TimeUnit.values()[_u.ordinal() - 1];
			long v2 = u2.convert(_d);
			double scale = _u.toNanos(1) / u2.toNanos(1);
			setValue(Double.valueOf(v2 / scale));
			if (v == 0)
				setFmt("#0.000");
			else if (av < 10)
				setFmt("#0.00");
			else
				setFmt("#0.0");
		} else if ((av > 200) && ((_u == TimeUnit.SECONDS) || (_u == TimeUnit.MINUTES))) {
			TimeUnit u2 = TimeUnit.values()[_u.ordinal() + 1];
			long v2 = u2.convert(_d);
			double scale = _u.toNanos(1) / u2.toNanos(1);
			setValue(Double.valueOf(v2 / scale));
			setFmt("#0.00");
		} else {
			setValue(Long.valueOf(v));
			setFmt("#00");
		}
			
		return SKIP_BODY;
	}
}