<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Accomplishment Eligibility - ${pilot.name}</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
 <td colspan="3" class="left"><span class="nophone"><content:airline /> </span>ACCOMPLISHMENT ELIGIBILITY FOR <span class="nophone">${pilot.name} </span><c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:20%">ACCOMPLISHMENT</td>
 <td class="nophone" style="width:20%">REQUIREMENT</td>
 <td>PROMOTION ELIGIBILITY</td>
</tr>

<!-- Table Data -->
<c:forEach var="acc" items="${fn:keys(accs)}">
<c:set var="msg" value="${accs[acc]}" scope="page" />
<view:row entry="${msg}">
 <td class="bld"><fmt:accomplish accomplish="${acc}" /></td>
 <td class="sec nophone"><fmt:int value="${acc.value}" /> ${acc.unit.name}</td>
<c:choose>
<c:when test="${msg.achieved}">
 <td class="pri bld left">${pilotName} achieved this Accomplishment on <fmt:date fmt="d" date="${acc.date}" />.</td>
</c:when>
<c:when test="${acc.unit == 'EQLEGS'}">
 <td class="left">${pilotName} achieved <fmt:int value="${msg.progress}" /> of the <fmt:int value="${acc.value}" /> Flight Legs in the
<fmt:list value="${acc.choices}" delim=", " /> required to achieve this Accomplishment.</td>
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
