<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> SELCAL Code Updated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="maxCodes" name="users.selcal.max" default="${1}" />

<!-- Main Body Frame -->
<content:region id="main">
<c:choose>
<c:when test="${isReserve}">
<div class="updateHdr">SELCAL CODE RESERVED</div>
<br />
The SELCAL code <span class="pri bld">${sc.code}</span> for aircraft registration ${sc.aircraftCode} has 
been reserved for you. This registration will automatically expire on <fmt:date date="${releaseDate}" />.<br />
<br />
You have reserved <fmt:int value="${codes}" /> SELCAL codes out of a maximum of <fmt:int value="${maxCodes}" /> 
at any one time.<br />
</c:when>
<c:when test="${isFree}">
<div class="updateHdr">SELCAL CODE RELEASED</div>
<br />
The SELCAL code <span class="pri bld">${sc.code}</span> for aircraft registration ${sc.aircraftCode} has 
been released for use by other <content:airline /> pilots.<br />
</c:when>
</c:choose>
<br />
To return to the list of available SELCAL codes, <el:cmd url="selcals" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
