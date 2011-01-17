<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Accomplishment Updated</title>
<content:pics />
<content:css name="main" browserSpecific="true" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isNew}">
<div class="updateHdr">New <content:airline /> Pilot Accomplishment Saved</div>
<br />
The <content:airline /> Pilot Accomplishment <fmt:accomplish className="bld" accomplish="${ap}" /> has been saved
in the database. This Accomplishment has <span class="bld">NOT</span> been awarded to any <content:airline /> Pilots.<br />
</c:when>
<c:otherwise>
<div class="updateHdr"><content:airline /> Pilot Accomplishment Updated</div>
<br />
The <content:airline /> Pilot Accomplishment <fmt:accomplish className="bld" accomplish="${ap}" /> has been updated
in the database.<br />
</c:otherwise>
</c:choose>
<br />
To calculate which Pilots are eligible for this Accomplishment, <el:cmd url="accomplishrecalc" link="${ap}" className="sec bld">Click Here</el:cmd>.<br />
To return to the list of Pilot Accomplishments, <el:cmd url="accomplishments" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
