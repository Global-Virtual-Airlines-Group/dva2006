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
<title><content:airline /> ACARS Message Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<content:js name="acarsLog" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarslogm.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4">ACARS TEXT MESSAGE LOG</td>
</tr>
<tr>
 <td class="label">Pilot Code</td>
 <td class="data"><el:text name="pilotCode" idx="*" size="7" max="8" value="${param.pilotCode}" /></td>
 <td class="label">Maximum Results</td>
 <td class="data"><el:text name="viewCount" idx="*" size="3" max="4" value="${param.viewCount}" /></td>
</tr>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${param.startDate}" />&nbsp;
<el:text name="startTime" idx="*" size="8" max="8" value="${param.startTime}" />&nbsp;
<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" /></td>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${param.endDate}" />&nbsp;
<el:text name="endTime" idx="*" size="8" max="8" value="${param.endTime}" />&nbsp;
<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" /></td>
</tr>
<tr>
 <td class="label">Search String</td>
 <td class="data" colspan="3"><el:text name="searchStr" idx="*" size="32" max="64" value="${param.searchStr}" /></td>
</tr>
<c:if test="${!empty system_message}">
<tr class="pri mid error">
 <td colspan="4">${system_message}</td>
</tr>
</c:if>
<content:filter roles="HR"><c:if test="${doSearch && (!empty rangeStart)}">
<tr class="mid">
 <td colspan="4">To view all ACARS Message Log entries in Microsoft Excel, <el:link url="acars_chat_log.ws?start=${rangeStart}&end=${rangeEnd}" className="sec bld">Click Here</el:link>.</td>
</tr>
</c:if></content:filter>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH TEXT MESSAGE LOG" /></td>
</tr>
</el:table>
</el:form>

<c:choose>
<c:when test="${!empty viewContext.results}">
<!-- Table Log Results -->
<view:table className="view" space="default" pad="default" cmd="acarslogm">
<tr class="title">
 <td colspan="4" class="left caps">TEXT MESSAGES</td>
</tr>
<c:forEach var="msg" items="${viewContext.results}">
<tr>
 <td colspan="4" class="left"><fmt:text value="${msg}" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="4"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</c:when>
<c:when test="${doSearch}">
<el:table className="view" space="default" pad="default">
<tr>
 <td class="pri bld">No Messages matching your search criteria were found in the ACARS log database.</td>
</tr>
</el:table>
</c:when>
</c:choose>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
