// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.util.function.Predicate;

/**
 * A Schedule Entry filter, for handling codeshares. This works as a two-pass filter; the first check is the flight is opreated
 * by an allowed Operator. The second check is if the codeshare code(s) match one of the code share operator codes. An empty set
 * of Operator code means that all Operators are allowed; <i>however this has the effect of removing all non-codeshare flights</i>.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class CodeShareFilter implements Predicate<ScheduleEntry> {
	
	private final Collection<String> _operatorCodes = new HashSet<String>();
	private final Collection<String> _codeShareCodes = new HashSet<String>();

	/**
	 * Creates the Filter, allowing flights for a set of Operators, or their code shares.
	 * @param csCodes a Collection of Airline IATA codes to allow
	*/
	public CodeShareFilter(Collection<String> csCodes) {
		this (csCodes, csCodes);
	}
	
	/**
	 * Creates the Filter.
	 * @param operatorCodes a Collection of operator Airline IATA codes to allow
	 * @param csCodes a Collection of codeshare Airline IATA codes to allow 
	 */
	public CodeShareFilter(Collection<String> operatorCodes, Collection<String> csCodes) {
		super();
		_operatorCodes.addAll(operatorCodes);
		_codeShareCodes.addAll(csCodes);
	}
	
	/**
	 * Returns the Operator Airline codes for this filter.
	 * @return a list of Operator IATA codes
	 */
	public Collection<String> getOperatorCodes() {
		return _operatorCodes;
	}
	
	/**
	 * Returns the Marketer Airline codes for this filter.
	 * @return a list of Marketer IATA codes
	 */
	public Collection<String> getMarketerCodes() {
		return _codeShareCodes;
	}
	
	@Override
	public boolean test(ScheduleEntry se) {
		if (se == null) return false;
		if (_operatorCodes.contains(se.getAirline().getCode())) return true; // if we're by a primary oeperator, we're good
		if (!se.isCodeShare()) return false;
		
		// Check codeshare operators
		Collection<String> csOperators = se.getCodeShareOperators();
		for (String csCode : _codeShareCodes)
			if (csOperators.contains(csCode)) return true;
		
		return false;
	}
}