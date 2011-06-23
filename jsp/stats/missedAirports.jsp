<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Unvisited Airports - ${pilot.name}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="allAirlines" name="airlines" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" cmd="newairports">
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> UNVISITED AIRPORTS FOR ${pilot.name}</td>
</tr>
<c:forEach var="entry" items="${airports}">
<c:set var="airline" value="${entry.key}" scope="page" />
<c:set var="aps" value="${entry.value}" scope="page" />
<tr class="title">
 <td colspan="4" class="left caps">${airline.name} - <fmt:int value="${fn:sizeof(aps)}" /> AIRPORTS</td>
</tr>
<c:forEach var="ap" items="${aps}">
<tr>
 <td width="25%" class="pri bld">${ap.name}</td>
 <td width="10%" class="bld">${ap.icao}</td>
 <td width="10%" class="sec">${ap.iata}</td>
 <td width="25%">${ap.country.name} <el:flag countryCode="${ap.country.code}" caption="${ap.country.name}" /></td>
 <td class="left small">
<c:forEach var="alCode" items="${ap.airlineCodes}" varStatus="acStatus">
<c:set var="apAirline" value="${allAirlines[alCode]}" scope="page" />
${apAirline.name}><c:if test="${!acStatus.last}">, </c:if></c:forEach></td>
</tr>
</c:forEach>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="4">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
