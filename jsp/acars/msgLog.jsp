<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
<content:js name="common" />
<content:js name="datePicker" />
<script language="JavaScript" type="text/javascript">
function valdiate(form)
{
if (!checkSubmit()) return false;

setSubmit();
disableButton('SearchButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="acarslogm.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4">ACARS TEXT MESSAGE LOG</td>
</tr>
<tr>
 <td class="label">Search Type</td>
 <td class="data" colspan="3"><el:check name="searchType" type="radio" idx="*" options="${searchTypes}" value="${searchType}" /></td>
</tr>
<tr>
 <td class="label">Pilot Code</td>
 <td class="data" colspan="3"><el:text name="pilotCode" idx="*" size="7" max="8" value="${param.pilotCode}" /></td>
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
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH TEXT MESSAGE LOG" />
</tr>
</el:table>
</el:form>

<c:choose>
<c:when test="${!empty viewContext.results}">
<!-- Table Log Results -->
<view:table className="view" space="default" pad="default" cmd="acarsloc">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="15%">FROM</td>
 <td width="15%">TO</td>
 <td>MESSAGE TEXT</td>
</tr>

<!-- Log Entries -->
<c:forEach var="msg" items="${viewContext.results}">
<c:set var="author" value="${pilots[msg.authorID]}" scope="request" />
<c:set var="authorLoc" value="${userData[msg.authorID]}" scope="request" />
<view:row entry="${msg}">
 <td class="small pri bld"><el:profile location="${authorLoc}">${author.name}</el:profile></td>
<c:if test="${msg.recipientID > 0}">
<c:set var="recipient" value="${pilots[msg.recipientID]}" scope="request" />
<c:set var="recipientLoc" value="${userData[msg.recipientID]}" scope="request" />
 <td class="small bld"><el:profile location="${recipientLoc}">${recipient.name}</el:profile></td>
 <td class="left small">${msg.message}</td>
</c:if>
<c:if test="${msg.recipientID > 0}">
 <td colspan="2" class="left small">${msg.message}</td>
</c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="3"><view:pgUp />&nbsp;<view:pgDn /></td>
</tr>
</view:table>
</c:when>
<c:otherwise>
<el:table className="view" space="default" pad="default">
<tr>
 <td class="pri bld">No Messages matching your search criteria were found in the ACARS log database.</td>
</tr>
</el:table>
</c:otherwise>
</c:choose>
<content:copyright />
</div>
</body>
</html>
