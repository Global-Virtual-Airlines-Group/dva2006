<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Inactivity Purge Preview</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="inactivelist">
<tr class="title caps">
 <td colspan="6"><content:airline /> PILOT INACTIVITY PURGE PREVIEW - <fmt:int value="${fn:sizeof(fn:keys(results))}" /> PILOTS</td>
</tr>
<c:if test="${!empty results}">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">CODE</td>
 <td width="20%">PILOT NAME</td>
 <td width="15%">FLIGHTS</td>
 <td width="5%">LOGINS</td>
 <td width="10%">LAST LOGIN</td>
 <td>PURGE REASON</td>
</tr>
</c:if>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${fn:keys(results)}">
<view:row entry="${pilot}">
<c:if test="${empty pilot.pilotCode}">
 <td class="pri bld">N / A</td>
</c:if>
<c:if test="${!empty pilot.pilotCode}">
 <td class="pri bld">${pilot.pilotCode}</td>
</c:if>
 <td><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
 <td><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td><fmt:int value="${pilot.loginCount}" /></td>
 <td class="bld"><fmt:date date="${pilot.lastLogin}" default="-" fmt="d" /></td>
 <td class="left">${results[pilot]}</td>
</view:row>
</c:forEach>

<!-- Legend Bar -->
<tr class="title">
 <td colspan="6"><view:legend width="100" labels="Active,On Leave" classes=" ,warn" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
