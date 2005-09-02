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
<title><content:airline /> System Diagnostics</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<!-- System Data Table -->
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">SYSTEM INFORMATION</td>
</tr>
<tr>
 <td class="label">Java Virtual Machine</td>
 <td class="data">${sys['java.runtime.name']} v${sys['java.runtime.version']} (${sys['java.vm.name']})</td>
</tr>
<tr>
 <td class="label">Operating System</td>
 <td class="data">${sys['os.name']} v${sys['os.version']}, (${sys['os.arch']} platform)</td>
</tr>
<tr>
 <td class="label">Server Information</td>
 <td class="data">${serverInfo}</td>
</tr>
<tr>
 <td class="label">Application Name</td>
 <td class="data">${servletContextName}</td>
</tr>
<tr>
 <td class="label">Servlet API</td>
 <td class="data">Version ${majorServletAPI}.${minorServletAPI}</td>
</tr>
<tr>
 <td class="label">CPU Count</td>
 <td class="data"><fmt:int value="${cpuCount}" /></td>
</tr>
<tr>
 <td class="label">Total JVM Memory</td>
 <td class="data"><fmt:int value="${totalMemory}" /> bytes</td>
</tr>
<tr>
 <td class="label">Maximum JVM Memory</td>
 <td class="data"><fmt:int value="${maxMemory}" /> bytes</td>
</tr>
<tr>
 <td class="label">Local Time Zone</td>
 <td class="data">${timeZone.ID} - ${tzName}</td>
</tr>
</el:table>

<!-- JDBC Connection Pool Data Table -->
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td colspan="7" class="left">JDBC CONNECTION INFORMATION</td>
</tr>

<c:if test="${!empty jdbcPoolInfo}">
<!-- JDBC Data Title Bar -->
<tr class="title caps">
 <td width="10%">ID</td>
 <td width="15%">CONNECTION TYPE</td>
 <td width="10%">STATUS</td>
 <td width="10%">USE COUNT</td>
 <td width="15%">AVERAGE USAGE</td>
 <td width="15%">TOTAL USAGE</td>
 <td>LAST/CURRENT USAGE</td>
</tr>

<!-- JDBC Data -->
<c:forEach var="con" items="${jdbcPoolInfo}">
<view:row entry="con">
 <td class="pri bld"><fmt:int value="${con.ID}" /></td>
 <td class="sec">${con.system? 'System' : 'User'}</td>
 <td class="bld">${con.inUse ? 'In Use' : 'Available'}</td>
 <td><fmt:int value="${con.useCount}" /></td>
 <td class="bld"><fmt:dec value="${con.totalUse / con.useCount}" /> ms</td>
 <td><fmt:int value="${con.totalUse}" /> ms</td>
 <td><fmt:int value="${con.currentUse}" /> ms</td>
</view:row>
</c:forEach>
</c:if>

<c:if test="${empty jdbcPoolInfo}">
<!-- JDBC Connection Data not available -->
<tr>
 <td class="pri bld" colspan="6">JDBC CONNECTION POOL DATA IS NOT AVAILABLE</td>
</tr>
</c:if>
</el:table>

<!-- Scheduled Task Pool Data Table -->
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td colspan="7" class="left">SCHEDULED TASK INFORMATION</td>
</tr>

<c:if test="${!empty taskInfo}">
<!-- Scheduled Task Data Title Bar -->
<tr class="title caps">
 <td width="20%">TASK NAME</td>
 <td width="25%">CLASS NAME</td>
 <td width="10%">INTERVAL</td>
 <td widht="5%">RUN</td>
 <td width="15%">LAST RUN</td>
 <td width="15%">NEXT RUN</td>
 <td>RUN TIME</td>
</tr>

<!-- ScheduledTask Data -->
<c:forEach var="task" items="${taskInfo}">
<view:row entry="task">
 <td class="pri bld"><el:cmd url="taskexec" linkID="${task.ID}">${task.name}</el:cmd></td>
 <td class="small">${task.className}</td>
 <td><fmt:int value="${task.interval / 60}" /> min</td>
<c:if test="${empty task.lastStartTime}">
 <td>N/A</td>
</c:if>
<c:if test="${!empty task.lastStartTime}">
 <td class="small"><fmt:date fmt="dt" date="${task.lastStartTime}" /></td>
</c:if>
<c:if test="${task.enabled}">
 <td class="small"><fmt:int value="${task.runCount}" /></td>
 <td class="small"><fmt:date fmt="dt" date="${task.nextStartTime}" /></td>
 <td><fmt:int value="${task.lastRunTime}" /> ms</td>
</c:if>
<c:if test="${!task.enabled}">
 <td colspan="3" class="sec bld">TASK DISABLED</td>
</c:if>
</view:row>
</c:forEach>
</c:if>

<c:if test="${empty taskInfo}">
<!-- Scheduled Task Data not available -->
<tr>
 <td class="pri bld" colspan="6">SCHEDULED TASK DATA IS NOT AVAILABLE</td>
</tr>
</c:if>
</el:table>
<content:copyright />
</div>
</body>
</html>
