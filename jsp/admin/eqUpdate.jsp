<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Equipment Program Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Equipment Program Updated</div>
<br />
The Equipment Profile for the <span class="pri bld">${eqType.name}</span> program has been successfully
updated in the database.<br />
<br />
<c:if test="${isRename}">
This Equipment Profile has been renamed. It was formerly called the <span class="sec bld">${oldName}</span>
program, and all Pilots in this program have been updated.<br />
<br />
</c:if>
<c:if test="${!empty updatedPilots}">
The following <content:airline /> Pilots have had their equipment type ratings updated:<br />
<br />
<c:forEach var="pilot" items="${fn:keys(updatedRatings)}">
<c:set var="ratings" value="${updatedRatings[pilot]}" scope="page" />
${pilot.rank} <el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd> - 
added <fmt:list value="${ratings}" delim=", " />.<br />
</c:forEach>
<br />
</c:if>
To return to the list of Equipment Program profiles, <el:cmd url="eqtypes" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
