// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file.innovata;

import java.util.ArrayList;
import java.util.List;

class CSVTokens implements Comparable {

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

	public List<String> getAll() {
		return _tkns;
	}

	public int size() {
		return _tkns.size();
	}
	
	public int compareTo(Object o2) {
		CSVTokens t2 = (CSVTokens) o2;
		int tmpResult = _tkns.get(7).compareTo(t2.get(7));
		if (tmpResult == 0)
			tmpResult = (_tkns.get(10).compareTo(t2.get(10)) * -1);
		
		return (tmpResult == 0) ? _tkns.get(4).compareTo(t2.get(4)) : tmpResult; 
	}
}