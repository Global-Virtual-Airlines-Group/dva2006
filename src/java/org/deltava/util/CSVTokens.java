// Copyright 2006, 2009, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

/**
 * A utility class to parse CSV data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class CSVTokens {

	private final ArrayList<String> _tkns = new ArrayList<String>(64);

	CSVTokens(String entry) {
		super();
		
		// Loop through the string
		int x = 0;
		while (x < entry.length()) {
			// Parse numeric or string
			if (entry.charAt(x) == '\"') {
				int ofs = entry.indexOf('\"', x + 1);
				if (ofs == -1) {
					_tkns.add(entry.substring(x + 1));
					x = entry.length();
				} else {
					_tkns.add(entry.substring(x + 1, ofs));
					x = ofs + 2;
				}
			} else {
				int ofs = entry.indexOf(',', x + 1);
				if (ofs == -1) {
					_tkns.add(entry.substring(x + 1));
					x = entry.length();
				} else {
					_tkns.add(entry.substring(x, ofs));
					x = ofs + 1;
				}
			}
		}
	}
	
	public String get(int ofs) {
		return _tkns.get(ofs);
	}
	
	public int size() {
		return _tkns.size();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (Iterator<String> i = _tkns.iterator(); i.hasNext(); ) {
			buf.append('\"');
			buf.append(i.next());
			buf.append('\"');
			if (i.hasNext())
				buf.append(',');
		}
		
		return buf.toString();
	}
}