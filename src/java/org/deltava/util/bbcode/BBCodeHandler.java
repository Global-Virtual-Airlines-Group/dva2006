package org.deltava.util.bbcode;
/*
 * Copyright (c) JForum Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided 
 * that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above 
 * copyright notice, this list of conditions and the 
 * following  disclaimer.
 * 2)  Redistributions in binary form must reproduce the 
 * above copyright notice, this list of conditions and 
 * the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * 3) Neither the name of "Rafael Steil" nor 
 * the names of its contributors may be used to endorse 
 * or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 * 
 * This file creation date: 03/08/2003 / 05:28:03
 * The JForum Project
 * http://www.jforum.net
 */

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.*;

import org.apache.log4j.Logger;

import org.deltava.util.ConfigLoader;

/**
 * @author Rafael Steil
 * @version $Id: BBCodeHandler.java,v 1.19 2007/07/28 14:17:09 rafaelsteil Exp $
 */
public class BBCodeHandler extends DefaultHandler
{
	private static final Logger log = Logger.getLogger(BBCodeHandler.class);
	
	private final Map<String, BBCode> bbMap = new LinkedHashMap<String, BBCode>();
	private String tagName = "";
	private StringBuilder sb;	
	private BBCode bb;
	
	public void init() {
		if (!bbMap.isEmpty())
			return;
		
		try (InputStream is = ConfigLoader.getStream("/etc/bb_config.xml")) {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, this);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Collection<BBCode> getAll() {
		return bbMap.values();
	}
	
	@Override
	public void startElement(String uri, String localName, String tag, Attributes attrs)
	{
		if (tag.equals("match")) {
			sb = new StringBuilder();
			bb = new BBCode();
			
			String tName = attrs.getValue("name");
			if (tName != null)
				bb.setTagName(tName);
			
			// Shall we remove the infamous quotes?
			String removeQuotes = attrs.getValue("removeQuotes");
			if (removeQuotes != null && removeQuotes.equals("true")) {
				bb.enableRemoveQuotes();
			}
			
			String alwaysProcess = attrs.getValue("alwaysProcess");
			if (alwaysProcess != null && "true".equals(alwaysProcess)) {
				bb.enableAlwaysProcess();
			}
		}
	
		this.tagName = tag;
	}

	@Override
	public void endElement(String uri, String localName, String tag)
	{	
		if (tag.equals("match"))
			bbMap.put(bb.getTagName(), bb);
		else if (tagName.equals("replace")) {
			bb.setReplace(sb.toString().trim());
			sb.delete(0, sb.length());
		}
		else if (tagName.equals("regex")) {
			bb.setRegex(sb.toString().trim());
			sb.delete(0, sb.length());
		}
	
		tagName = "";
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (tagName.equals("replace") || tagName.equals("regex"))
			sb.append(ch, start, length);
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		throw exception;
	}
}