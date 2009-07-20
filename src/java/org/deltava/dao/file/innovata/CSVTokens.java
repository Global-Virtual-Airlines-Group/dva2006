// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.util.*;

class CSVTokens implements Comparable<CSVTokens> {

	private int _lineNumber;
	private List<String> _tkns = new ArrayList<String>();

	CSVTokens(String entry, int line) {
		super();
		_lineNumber = line;
		
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
	
	public int getLineNumber() {
		return _lineNumber;
	}

	public String get(int ofs) {
		return _tkns.get(ofs);
	}
	
	public void set(int ofs, String value) {
		_tkns.set(ofs, value);
	}

	public List<String> getAll() {
		return _tkns;
	}

	public int size() {
		return _tkns.size();
	}
	
	public int compareTo(CSVTokens t2) {
		int tmpResult = _tkns.get(7).compareTo(t2.get(7)); // flight number
		if (tmpResult == 0)
			tmpResult = (_tkns.get(10).compareTo(t2.get(10)) * -1); // stops
		if (tmpResult == 0)
			tmpResult = _tkns.get(4).compareTo(t2.get(4)); // departure time
		if (tmpResult == 0)
			tmpResult = _tkns.get(0).compareTo(t2.get(0)); // start date
		
		return (tmpResult == 0) ? _tkns.get(1).compareTo(t2.get(1)) : tmpResult; // end date 
	}
	
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