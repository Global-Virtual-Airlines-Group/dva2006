// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import jakarta.servlet.jsp.*;

import org.deltava.beans.stats.RunwayLandingStats;

import org.deltava.taglib.html.ElementTag;

/**
 * A JSP tag to display formatted relative Landing Score comments. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class LandingScoreCommentTag extends ElementTag {

	private double _score;
	private RunwayLandingStats _stats;
	
	private boolean _isWorse;
	
	/**
	 * Creates the tag.
	 */
	public LandingScoreCommentTag() {
		super("span");
	}
	
	/**
	 * Updates the landing score.
	 * @param ls the landing score
	 */
	public void setScore(double ls) {
		_score = ls;
	}
	
	/**
	 * Updates the landing score statistics for this Runway.
	 * @param rls a RunwayLandingStats bean
	 */
	public void setStats(RunwayLandingStats rls) {
		_stats = rls;
	}
	
	@Override
	public int doStartTag() throws JspException {
		if (_stats == null) return SKIP_BODY;
		super.doStartTag();
		
		// Set style based on score
		_isWorse = (_score < _stats.getAverageScore());
		_classes.add(_isWorse ? "warn" : "ok");
		
		// Open the span
		try {
			_out.print(_data.open(true));
		} catch (Exception e) {
			throw new JspException(e);			
		}
		
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() throws JspException {
		if (_stats == null) return EVAL_PAGE;
		
		// Check if within standard deviation
		double scoreDelta = Math.abs(_score - _stats.getAverageScore());
		boolean withinSD = (scoreDelta <= _stats.getScoreSD());
		boolean withinMP = (scoreDelta <= (_stats.getScoreSD()/ 2));
		
		try {
			StringBuilder label = new StringBuilder(_isWorse ? "worse" : "better");
			
			if (!withinSD) {
				_out.print("Substantially ");
				label.setCharAt(0, Character.toUpperCase(label.charAt(0)));
			}
			
			if (!withinMP) {
				_out.print(label);
				_out.print(" than ");
			}
			
			_out.print("Average");
			_out.print(_data.close());
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return super.doEndTag();
	}
}