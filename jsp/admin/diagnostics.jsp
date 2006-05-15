<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
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
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
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
 <td class="label">Application Started on</td>
 <td class="data"><fmt:date date="${startedOn}" /> (<fmt:int value="${execTime / 60}" /> minutes)</td>
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
 <td class="label">JVM Memory</td>
 <td class="data"><fmt:int value="${totalMemory}" /> bytes in use, <fmt:int value="${maxMemory}" />
 bytes maximum. <c:choose><c:when test="${pctMemory > 90}"><span class="error bld"></c:when>
<c:when test="${pctMemory > 80}"><span class="warn bld"></c:when>
<c:otherwise><span class="bld"></c:otherwise></c:choose>(<fmt:dec value="${pctMemory}" />% used)</span> 
Free Memory: <fmt:int value="${freeMemory}" /> bytes</td>
</tr>
<tr>
 <td class="label">Local Time Zone</td>
 <td class="data">${timeZone.ID} - ${tzName}</td>
</tr>
<tr>
 <td class="label">Database Transactions</td>
 <td class="data"><fmt:int value="${daoUsageCount}" /> queries, (<fmt:dec value="${(daoUsageCount * 60) / execTime}" />
 per minute)</td>
</tr>
<tr>
 <td class="label">Google Maps API Usage</td>
 <td class="data"><fmt:int value="${mapsAPIUsage}" /> times since application start</td>
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
<view:row entry="${con}">
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

<!-- Servlet Scoreboard -->
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td colspan="4" class="left">SERVLET SCOREBOARD</td>
</tr>

<!-- Scoreboard Title Bar -->
<tr class="title caps">
 <td width="10%">THREAD NAME</td>
 <td width="35%">REMOTE ADDRESS</td>
 <td width="20%">EXECUTION TIME</td>
 <td class="left">REQUEST URI</td>
</tr>

<!-- Scoreboard Data -->
<c:forEach var="thread" items="${scoreBoard}">
<view:row entry="${thread}">
 <td class="pri small bld">${thread.name}</td>
 <td class="small">${thread.remoteHost} (${thread.remoteAddr})</td>
 <td class="sec small"><fmt:date fmt="dt" d="MM/dd" t="HH:mm:ss" date="${thread.startTime}" /> (<fmt:int value="${thread.executionTime}" /> ms)</td>
 <td class="small left">${thread.URL}</td>
</view:row>
</c:forEach>
</el:table>

<c:if test="${!empty servInfoStatus}">
<!-- ServInfo network status information -->
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td colspan="6" class="left">ONLINE NETWORK STATUS DATA SOURCES</td>
</tr>

<!-- ServInfo Title Bar -->
<tr class="title caps">
 <td width="45%">DATA SOURCE URL</td>
 <td width="15%">LAST USE</td>
 <td width="10%">SUCCESS</td>
 <td width="10%">FAILURES</td>
 <td width="10%">TOTAL USAGE</td>
 <td>PERCENTAGE</td>
</tr>

<c:forEach var="netStatus" items="${servInfoStatus}">
<!-- ${netStatus.name} -->
<tr class="title caps">
 <td colspan="6" class="left">${netStatus.name} - ${fn:sizeof(netStatus.URLs)} SOURCES</td>
</tr>
<c:forEach var="source" items="${netStatus.URLs}">
<tr>
 <td class="pri bld">${source.URL}</td>
 <td class="sec"><fmt:date date="${source.lastUse}" default="NEVER" /></td>
 <td><fmt:int value="${source.success}" /></td>
 <td><fmt:int value="${source.failures}" /></td>
 <td><fmt:int value="${source.success + source.failures}" /></td>
 <td><fmt:int value="${source.successPercentage}" />%</td>
</tr>
</c:forEach>
</c:forEach>
</el:table>
</c:if>

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
 <td width="5%">RUN</td>
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
<c:if test="${task.enabled}">
 <td class="small"><fmt:int value="${task.runCount}" /></td>
 <td class="small"><fmt:date fmt="dt" date="${task.lastStartTime}" /></td>
 <td class="small"><fmt:date fmt="dt" date="${task.nextStartTime}" /></td>
 <td><fmt:int value="${task.lastRunTime}" /> ms</td>
</c:if>
<c:if test="${!task.enabled}">
 <td colspan="4" class="sec bld">TASK DISABLED</td>
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
<br />
<c:if test="${!empty acarsPool}">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td class="left caps" colspan="7">ACARS CONNECTION POOL INFORMATION</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="10%">ID</td>
 <td width="18%">USER</td>
 <td width="10%">FLIGHT ID</td> 
 <td width="27%">REMOTE ADDRESS</td>
 <td width="15%">MESSAGES</td>
 <td>BYTES</td>
</tr>

<!-- Table Connection Data -->
<c:forEach var="con" items="${acarsPool}">
<tr>
 <td class="priB"><fmt:hex value="${con.ID}" /></td>
 <td class="pri bld"><el:cmd url="profile" linkID="0x${con.user.ID}">${con.user.name}</el:cmd></td>
 <td class="sec bld">${(con.flightID == 0) ? 'N/A' : con.flightInfo.flightCode}</td>
 <td class="small">${con.remoteAddr} (${con.remoteHost})</td>
 <td><fmt:int value="${con.msgsIn}" /> in, <fmt:int value="${con.msgsOut}" /> out</td>
 <td><fmt:int value="${con.bytesIn}" /> in, <fmt:int value="${con.bytesOut}" /> out</td>
</tr>
<tr>
 <td colspan="4" class="small">Socket: <fmt:int value="${con.socket.sendBufferSize}" /> bytes 
out, <fmt:int value="${con.socket.receiveBufferSize}" /> bytes in. NODELAY=${con.socket.tcpNoDelay}, 
KEEPALIVE=${con.socket.keepAlive}</td>
 <td colspan="2" class="small">Buffer: <fmt:int value="${con.bufferWrites}" /> writes, 
<fmt:dec value="${con.bufferWrites / con.msgsOut}" fmt="#0.00" /> per message, 
<fmt:dec value="${con.bytesOut / con.bufferWrites}" fmt="###0.0" /> avg. bytes</td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="#{!empty acarsBans}">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td class="left caps" colspan="7">ACARS BLOCKED USERS</td>
</tr>

<!-- Table Header Data -->
<tr class="title">
 <td width="35%">REMOTE ADDRESS</td>
 <td width="25%">PILOT NAME</td>
 <td width="10%">PILOT CODE</td>
 <td width="15%">RANK</td>
 <td>EXPIRATION DATE</td>
</tr>

<!-- Table Ban Data -->
<c:forEach var="ban" items="${acarsBans}">
<tr>
 <td class="small">${ban.remoteHost} (${ban.remoteAddr})</td>
<c:if test="${!empty ban.userData}">
 <td><el:profile className="bld" location="${ban.userData}">${ban.user.name}</el:profile></td>
 <td class="pri bld">${ban.user.pilotCode}</td>
 <td>${ban.user.rank}, ${ban.user.equipmentType}</td>
</c:if> 
<c:if test="${empty ban.userData}">
 <td colspan="3" class="bld">ANONYMOUS USER</td>
</c:if>
 <td><fmt:date date="${ban.expiryDate}" /></td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty workers}">
<!-- ACARS Server Worker threads -->
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td class="left caps" colspan="4">ACARS WORKER THREAD INFORMATION</td>
</tr>

<!-- Table Header Bar-->
<tr class="title">
 <td width="30%">THREAD NAME</td>
 <td width="15%">THREAD STATUS</td>
 <td width="10%">EXECUTION COUNT</td>
 <td>CURRENTLY EXECUTING</td>
</tr>

<!-- Table Thread Data -->
<c:forEach var="worker" items="${workers}">
<view:row entry="${worker}">
 <td class="pri bld">${worker}</td>
 <td class="sec">${worker.statusName}</td>
 <td><fmt:int value="${worker.executionCount}" /></td>
 <td class="left">${worker.message}</td>
</view:row>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty acarsCmdStats}">
<!-- ACARS Server Command Statistics -->
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td class="left caps" colspan="6">ACARS SERVER STATISTICS</td>
</tr>

<!-- Command Statistics Header -->
<tr class="title">
 <td width="30%">COMMAND NAME</td>
 <td width="10%">INVOCATIONS</td>
 <td width="15%">AVERAGE TIME</td>
 <td width="15%">TOTAL TIME</td>
 <td width="15%">MAXIMUM TIME</td>
 <td>MINIMUM TIME</td>
</tr>

<!-- Command Statistics Data -->
<c:forEach var="cmdStat" items="${acarsCmdStats}">
<tr>
 <td class="pri bld">${cmdStat.name}</td>
 <td><fmt:int value="${cmdStat.count}" /></td>
 <td class="bld"><fmt:int value="${cmdStat.totalTime / cmdStat.count}" /> ms</td>
 <td><fmt:int value="${cmdStat.totalTime}" /> ms</td>
 <td><fmt:int value="${cmdStat.maxTime}" /> ms</td>
 <td><fmt:int value="${cmdStat.minTime}" /> ms</td>
</tr>
</c:forEach>

<!-- Table Statistics Data -->
<c:set var="statIdx" value="${0}" scope="request" />
<c:forEach var="stat" items="${acarsStatNames}">
<c:set var="statIdx" value="${statIdx + 1}" scope="request" />
<tr>
 <td colspan="6" class="left"><span class="pri bld">${stat}</span> <fmt:int value="${acarsStats[statIdx]}" /></td>
</tr>
</c:forEach>
</el:table>
<br />
</c:if>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
