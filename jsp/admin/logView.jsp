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
<title><content:airline /> System Log</title>
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
<content:sysdata var="logNames" name="log.names" sort="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="systemlog.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4">BROWSE SYSTEM LOGS</td>
</tr>
<tr>
 <td class="label">Log Name</td>
 <td class="data"><el:combo name="id" idx="*" size="1" options="${logNames}" value="${param.id}" /></td>
 <td class="label">Priority</td>
 <td class="data"><el:combo name="priority" idx="*" size="1" options="${priorities}" value="${param.priority}" /></td>
</tr>
<tr>
 <td class="label">Start Date/Time</td>
 <td class="data"><el:text name="startDate" idx="*" size="10" max="10" value="${param.startDate}" />&nbsp;
<el:text name="startTime" idx="*" size="5" max="5" value="${param.startTime}" />&nbsp;
<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].startDate')" /></td>
 <td class="label">End Date/Time</td>
 <td class="data"><el:text name="endDate" idx="*" size="10" max="10" value="${param.endDate}" />&nbsp;
<el:text name="endTime" idx="*" size="5" max="5" value="${param.endTime}" />&nbsp;
<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].endDate')" /></td>
</tr>
<tr>
 <td class="label">Logger Class Name</td>
 <td class="data" colspan="3"><el:text name="loggerClass" idx="*" size="64" max="128" value="${param.loggerClass}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH SYSTEM LOG" /></td>
</tr>
</el:table>
</el:form>

<c:if test="${!empty viewContext.results}">
<view:table className="view" pad="default" space="default" cmd="systemlog">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">ID</td>
 <td width="15%">DATE/TIME</td>
 <td width="20%">LOGGER CLASS</td>
 <td>LOG MESSAGE</td>
</tr>

<!-- Table Log Data -->
<c:forEach var="entry" items="${viewContext.results}">
<view:row entry="${entry}">
 <td class="sec bld"><fmt:int value="${entry.ID}" /></td>
 <td><fmt:date fmt="dt" date="${entry.date}" /></td>
 <td>${entry.name}</td>
 <td class="left"><fmt:text value="${entry.message}" /></td>
</view:row>
<c:if test="${!empty entry.error}">
<view:row entry="${entry}">
 <td class="left" colspan="4"><fmt:text value="${entry.error}" /></td>
</view:row>
</c:if>
</c:forEach>

<!-- Scroll/Legend Bar -->
<tr class="title">
 <td colspan="4"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br /></view:scrollbar>
<view:legend width="100" labels="Debug,Info,Warning,Error,Fatal" classes="opt2, ,warn,err,err" /></td>
</tr>
</view:table>
</c:if>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
