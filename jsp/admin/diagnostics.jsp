<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" buffer="32kb" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_diag.tld" prefix="diag" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<content:sysdata var="acarsEnabled" name="acars.enabled" />
<content:sysdata var="discordEnabled" name="discord.bot" />
<head>
<title><content:airline /> System Diagnostics</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
<content:js name="common" />
<c:if test="${acarsEnabled}"><content:googleJS module="charts" /></c:if>
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<map:usage var="s_APIuse" type="static" />
<map:usage var="d_APIuse" type="dynamic" />
<c:set var="startedOn" value="${applicationScope.startedOn}" scope="page" />
<content:duration var="execTime" length="${(systemTime.toEpochMilli() - startedOn.toEpochMilli()) / 1000}" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
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
<c:if test="${!empty osStart}">
<tr>
 <td class="label">Server Started on</td>
 <td class="data"><fmt:date date="${osStart}" /> (<fmt:int value="${osExecTime / 86400}" /> days)</td>
 </tr>
</c:if>
<c:if test="${!empty loadAvg}">
<tr>
 <td class="label top">Load Average</td>
 <td class="data">Last 1 Minute: <fmt:dec value="${fn:get(loadAvg, 0)}" /> processes queued<br />
 Last 5 Minutes: <fmt:dec value="${fn:get(loadAvg, 1)}" /> processes queued<br />
 Last 15 Minutes: <fmt:dec value="${fn:get(loadAvg, 2)}" /> processes queued</td>
</tr>
</c:if>
<c:if test="${!empty osMemInfo}">
<tr>
 <td class="label top">Memory Usage</td>
 <td class="data"><fmt:fileSize value="${osMemInfo['MemTotal']}" /> KB total physical memory<br />
<fmt:fileSize value="${osMemInfo['MemFree']}" /> KB free physical memory</td>
</tr>
</c:if>
<tr>
 <td class="label">Server Information</td>
 <td class="data">${pageContext.servletContext.serverInfo}</td>
</tr>
<tr>
 <td class="label">Application Name</td>
 <td class="data">${pageContext.servletContext.servletContextName}</td>
</tr>
<tr>
 <td class="label">Application Started on</td>
 <td class="data"><fmt:date date="${startedOn}" /> (<fmt:duration duration="${execTime}" />)</td>
</tr>
<c:if test="${!empty appNames}">
<tr>
 <td class="label">Web Application Names</td>
 <td class="data"><fmt:list value="${appNames}" delim=", " /></td>
</tr>
</c:if>
<tr>
 <td class="label">Servlet API</td>
 <td class="data">Version <diag:servlet_api /></td>
</tr>
<tr>
 <td class="label">JSP API</td>
 <td class="data">Version <diag:jsp_api /></td>
</tr>
<tr>
 <td class="label">CPU Count</td>
 <td class="data">${cpuCount} processors</td>
</tr>
<tr>
 <td class="label">JVM Memory</td>
 <td class="data"><fmt:int value="${totalMemory}" /> bytes in use, <fmt:int value="${maxMemory}" /> bytes maximum. <span class="bld">(<fmt:dec value="${pctMemory}" />% used)</span> 
Free Memory: <fmt:int value="${freeMemory}" /> bytes</td>
</tr>
<tr>
 <td class="label">Local Time Zone</td>
 <td class="data">${timeZone.id} - ${tzName}</td>
</tr>
<tr>
 <td class="label">MySQL Version</td>
 <td class="data pri bld">${dbVersion}</td>
</tr>
<tr>
 <td class="label">Database Transactions</td>
 <td class="data"><fmt:int value="${daoUsageCount}" /> queries, (<fmt:dec value="${daoUsageCount / execTime.toMinutes() }" /> per minute)</td>
</tr>
<c:if test="${!empty vkStatus}">
<c:set var="rawUptime" value="${vkStatus['uptime_in_seconds']}" scope="page" />
<content:duration var="uptime" length="${rawUptime.longValue()}" />
<tr>
 <td class="label top">Valkey Status</td>
 <td class="data">Server version: <span class="bld">${vkStatus['valkey_version']}</span> Uptime: <span class="ita"><fmt:duration duration="${uptime}" /></span><br />
<fmt:int value="${vkStatus['instantaneous_ops_per_sec']}" /> operations/sec, Memory <fmt:fileSize value="${vkStatus['used_memory']}" /> / <fmt:fileSize value="${vkStatus['maxmemory']}" /></td>
</tr>
</c:if>
<c:if test="${discordEnabled}">
<tr>
 <td class="label">Discord Status</td>
 <td class="data"><span class="pri bld">BOT ENABLED</span> - <span class="bld ${discordOK ? 'ter' : 'error'}">${discordOK ? 'OPERATIONAL' : 'NOT INITIALIZED'}</span></td>
</tr>
</c:if>
<tr>
 <td class="label">Google Maps API Usage</td>
 <td class="data"><fmt:int value="${d_APIuse}" /> dynamic, <fmt:int value="${s_APIuse}" /> static since application start</td>
</tr>
<c:if test="${!empty buildDataCore && !empty buildDataJSP}">
<tr>
 <td class="label top">Build Information</td>
 <td class="data"><span class="pri bld">Core</span> - Built on ${buildDataCore['build.date']} on Java ${buildDataCore['build.jvm']} / ${buildDataCore['build.os']}<br />
<span class="pri bld">JSP</span> - Built on ${buildDataJSP['build.date']} on Java ${buildDataJSP['build.jvm']} / ${buildDataJSP['build.os']}</td>
</tr>
</c:if>
</el:table>
<%@ include file="/jsp/admin/diag/jdbcPool.jspf" %>
<%@ include file="/jsp/admin/diag/daoCache.jspf" %>
<%@ include file="/jsp/admin/diag/schedTasks.jspf" %>
<%@ include file="/jsp/admin/diag/httpCompression.jspf" %>
<c:if test="${acarsEnabled}">
<%@ include file="/jsp/admin/diag/acarsInfo.jspf" %></c:if>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
