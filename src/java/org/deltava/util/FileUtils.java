// Copyright 2012, 2016, 2018, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

/**
 * A utility class for filesystem functions. 
 * @author Luke
 * @version 10.0
 * @since 4.2
 */

public class FileUtils {
	
	public static final FilenameFilter ACCEPT_ALL = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return true;
		}
	};

	public static final DirectoryStream.Filter<Path> IS_FILE = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path p) throws IOException {
			return Files.isRegularFile(p);
		}
	};
	
	public static final DirectoryStream.Filter<Path> IS_DIR = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path p) throws IOException {
			return Files.isDirectory(p);
		}
	};
	
	// static class
	private FileUtils() {
		super();
	}

	/**
	 * Returns the newest file in a directory.
	 * @param path the directory path
	 * @param ff a FilenameFilter to limit files selected
	 * @return the File, or null if empty
	 */
	public static File findNewest(String path, FilenameFilter ff) {
		File[] files = new File(path).listFiles(ff);
		if (files == null)
			return null;
		
		File newest = null;
		for (int x = 0; x < files.length; x++) {
			if ((newest == null) || (files[x].lastModified() > newest.lastModified()))
				newest = files[x];
		}
		
		return newest;
	}

	/**
	 * Utility method to get filenames with a particular prefix and extension.
	 * @param prefix the prefix
	 * @param ext the extension
	 * @return a FilenameFilter
	 */
	public static FilenameFilter fileFilter(String prefix, String ext) {
		final String e = (ext == null) ? "" : ext.toLowerCase().replace("*", "");
		final String p = (prefix == null) ? "" : prefix.toLowerCase().replace("*", "");
		
		return new FilenameFilter() {
	        @Override
			public boolean accept(File dir, String name) {
	            String n = name.toLowerCase();
	            return n.startsWith(p) && n.endsWith(e);
	          }};
	}
	
	/**
	 * Sets the owner and group membership for a file.
	 * @param f the File
	 * @param owner the owner name
	 * @param group the group name
	 * @throws IOException if an error occurs 
	 */
	public static void setOwner(File f, String owner, String group) throws IOException {
		Path p = f.toPath();
		UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
		if (!StringUtils.isEmpty(group)) {
			GroupPrincipal grp = lookupService.lookupPrincipalByGroupName(group);
			Files.setAttribute(p, "posix:group", grp, LinkOption.NOFOLLOW_LINKS);
		}
		
		if (!StringUtils.isEmpty(owner)) {
			UserPrincipal usr = lookupService.lookupPrincipalByName(owner);
			Files.setOwner(p, usr);
		}
	}
	
	/**
	 * Sets the permissions for a file.
	 * @param f the File
	 * @param permissions the permission names
	 * @throws IOException if an error occurs
	 * @see PosixFilePermission
	 */
	public static void setPermissions(File f, String... permissions) throws IOException {
		
		// Parse permission names
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		for (int x = 0; x < permissions.length; x++) 
			perms.add(PosixFilePermission.valueOf(permissions[x]));
		
		Files.setPosixFilePermissions(f.toPath(), perms);
	}
}