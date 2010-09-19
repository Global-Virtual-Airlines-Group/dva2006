<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Accomplishment Eligibility - ${pilot.name}</title>
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
<c:set var="pilotName" value="${isOurs ? 'You' : pilot.firstName}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view">
<tr class="title caps">
 <td colspan="3" class="left"><content:airline /> ACCOMPLISHMENT ELIGIBILITY FOR ${pilot.name} <c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="20%">ACCOMPLISHMENT</td>
 <td width="20%">REQUIREMENT</td>
 <td>PROMOTION ELIGIBILITY</td>
</tr>

<!-- Table Data -->
<c:forEach var="acc" items="${fn:keys(accs)}">
<c:set var="msg" value="${accs[acc]}" scope="page" />
<view:row entry="${msg}">
 <td class="bld"><fmt:accomplish accomplish="${acc}" /></td>
 <td class="sec"><fmt:int value="${acc.value}" /> ${acc.unit.name}</td>
<c:choose>
<c:when test="${msg.achieved}">
 <td class="pri bld left">${pilotName} achieved this Accomplishment on <fmt:date fmt="d" date="${acc.date}" />.</td>
</c:when>
<c:when test="${!empty msg.missing}">
 <td class="left">${pilotName} achieved <fmt:int value="${msg.progress}" /> of the <fmt:int value="${acc.value}" /> ${acc.unit.name} required to
 achieve this Accomplishment. The following ${acc.unit.name} are still required for this Accomplishment: 
<c:forEach var="item" items="${msg.missing}" varStatus="missingStatus"><c:choose>
<c:when test="${msg.missingClass.simpleName == 'State'}">${item.name}<c:if test="${!missingStatus.last}">, </c:if></c:when>
<c:when test="${msg.missingClass.simpleName == 'Airport'}">${item.name} (<fmt:airport airport="${item}" />)<c:if test="${!missingStatus.last}">, </c:if></c:when>
<c:when test="${msg.missingClass.simpleName == 'Country'}">${item.name}<c:if test="${!missingStatus.last}">, </c:if></c:when>
<c:when test="${msg.missingClass.simpleName == 'Airline'}">${item.name}<c:if test="${!missingStatus.last}">, </c:if></c:when>
<c:otherwise>${item}<c:if test="${!missingStatus.last}">, </c:if></c:otherwise>
</c:choose></c:forEach>.</td> 
</c:when>
<c:otherwise>
 <td class="left">${pilotName} achieved <fmt:int value="${msg.progress}" /> of the <fmt:int value="${acc.value}" /> ${acc.unit.name} required to achieve this Accomplishment.</td>
</c:otherwise>
</c:choose>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="3">&nbsp;</td>
</tr> 
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
