// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

/**
 * An enumeration to track combined EuroControl sectors.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public enum EuroControl {

	EURN("BIRD","EETT","EFES","EFPS","EKDK","ENBD","ENOB","ENOS","ENSV","ENTR","ESAA","EVRR","EYVL"),
	EURM("EBBU","EDBB","EDFF","EDMM","EDLL","EDWW","EHAA","LOVV","LSAS"),
	EURW("LECB","LECM","LFBB","LFEE","LFFF","LFMM","LFRR","LPPC","EISN"),
	EURE("EPWW","LAAA","LBSR","LBWR","LDZO","LHCC","LJLA","LKAA","LRBB","LWSS","LZBB","LQSB","LYBA","LUKK"),
	EURS("LCCC","LGGG","LIBB","LIMM","LIRR","LMMM","LTBB");
	
	private final Collection<String> _firs = new TreeSet<String>();
	
	EuroControl(String... firs) {
		_firs.addAll(Arrays.asList(firs));
	}

	/**
	 * Returns the FIR codes covered by this EuroControl sector.
	 * @return a Collection of FIR codes
	 */
	public Collection<String> getFIRs() {
		return new ArrayList<String>(_firs);
	}
}