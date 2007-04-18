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
<title><content:airline /> ACARS Connection Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<script language="JavaScript" type="text/javascript">
function validate(form)
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
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarslogc.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4">ACARS CONNECTION LOG</td>
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
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH CONNECTION LOG" />
</tr>
</el:table>
</el:form>

<c:choose>
<c:when test="${!empty viewContext.results}">
<!-- Table Log Results -->
<view:table className="view" space="default" pad="default" cmd="acarslogc">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">ID</td>
 <td width="15%">DATE/TIME</td>
 <td width="10%">PILOT CODE</td>
 <td width="20%">PILOT NAME</td>
 <td width="5%">BUILD</td>
 <td width="5%">TEXT</td>
 <td width="5%">INFO</td>
 <td width="8%">POS</td>
 <td>HOST NAME</td>
</tr>

<!-- Log Entries -->
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[entry.pilotID]}" scope="request" />
<c:set var="pilotLoc" value="${userData[entry.pilotID]}" scope="request" />
<view:row entry="${entry}">
 <td class="pri bld"><fmt:hex value="${entry.ID}" /></td>
 <td class="small"><fmt:date date="${entry.startTime}" /></td>
 <td class="sec bld">${pilot.pilotCode}</td>
 <td class="pri bld"><el:profile location="${pilotLoc}">${pilot.name}</el:profile></td>
 <td class="sec small bld"><fmt:int value="${entry.clientBuild}" /></td>
 <td><fmt:int value="${entry.messageCount}" /></td>
 <td><fmt:int value="${entry.flightInfoCount}" /></td>
 <td><fmt:int value="${entry.positionCount}" /></td>
 <td class="small">${entry.remoteHost} (${entry.remoteAddr})</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="9"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</c:when>
<c:when test="${doSearch}">
<el:table className="view" space="default" pad="default">
<tr>
 <td class="pri bld">No Connections matching your search criteria were found in the ACARS log database.</td>
</tr>
</el:table>
</c:when>
</c:choose>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
