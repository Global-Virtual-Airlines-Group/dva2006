<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Accomplishment Updated</title>
<content:pics />
<content:favicon />
<content:css name="main" />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
The <content:airline /> Pilot Accomplishment <fmt:accomplish className="bld" accomplish="${ap}" /> has been updated in the database.<br />
</c:otherwise>
</c:choose>
<br />
<c:if test="${!empty invalidAirports}">
The following airport codes were not recognized and have been stripped from the Pilot Accomplishment: <span class="error bld"><fmt:list value="${invalidAirports}" delim=", " /></span><br />
<br />
To modify this Accomplishment, <el:cmd url="accomplishment" link="${ap}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
To calculate which Pilots are eligible for this Accomplishment, <el:cmd url="accomplishrecalc" link="${ap}" className="sec bld">Click Here</el:cmd>.<br />
To return to the list of Pilot Accomplishments, <el:cmd url="accomplishments" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
