// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.system;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A secrets manager that uses properties files. 
 * @author Luke
 * @version 10.5
 * @since 10.5
 */

public class PropertiesSecretManager implements SecretManager {
	
	private final String _fileName;
	private final Properties _props = new Properties();

	/**
	 * Creates the Secret Manager.
	 * @param srcFile the source property file name
	 */
	public PropertiesSecretManager(String srcFile) {
		super();
		_fileName = srcFile;
	}
	
	@Override
	public Collection<String> getKeys() {
		return _props.keySet().stream().map(String::valueOf).collect(Collectors.toSet());
	}
	
	@Override
	public String get(String name) {
		return _props.getProperty(name);
	}
	
	@Override
	public int size() {
		return _props.size();
	}

	@Override
	public void load() throws IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(_fileName))) {
			_props.load(is);	
		}
	}
}