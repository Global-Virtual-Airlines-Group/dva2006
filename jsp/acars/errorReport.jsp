<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Client Error Report</title>
<content:expire expires="30" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">ACARS CLIENT ERROR REPORT INFORMATION - ERROR #<fmt:int value="${err.ID}" /></td>
</tr>
<tr>
 <td class="label">Error Message</td>
 <td class="data pri bld">${err.message}</td>
</tr>
<tr>
 <td class="label">Logged on</td>
 <td class="data"><fmt:date date="${err.createdOn}" /> by <el:profile location="${userData}"  className="pri bld">${author.name}</el:profile></td>
</tr>
<tr>
 <td class="label">Client Build</td>
 <td class="data">Build <fmt:int value="${err.clientBuild}" /><c:if test="${err.beta > 0}"> Beta <fmt:int value="${err.beta}" /></c:if></td>
</tr>
<tr>
 <td class="label">Operating System</td>
 <td class="data">${err.OSVersion} (${err.is64Bit ? '64' : '32'}-bit) - CLR ${err.CLRVersion}</td>
</tr>
<tr>
 <td class="label">Location Settings</td>
 <td class="data"><span class="sec bld">${err.locale}</span> ${err.timeZone}</td>
</tr>
<tr>
 <td class="label">Simulator</td>
 <td class="data bld">${err.simulator.name}</td>
</tr>
<c:if test="${!empty err.FSUIPCVersion}">
<tr>
 <td class="label">FSUIPC Version</td>
 <td class="data">${err.FSUIPCVersion}</td>
</tr>
</c:if>
<tr>
 <td class="label">Reported from</td>
 <td class="data">${err.remoteAddr} (${err.remoteHost}) <c:if test="${!empty ipInfo}">&nbsp;<el:flag countryCode="${ipInfo.country.code}" caption="${ipInfo.location}" />
 ${ipInfo.location}</c:if></td>
</tr>
<c:if test="${!empty err.stackDump}">
<tr>
 <td class="label top">Error Information</td>
 <td class="data"><fmt:text value="${err.stackDump}" /></td>
</tr>
</c:if>
<c:if test="${!empty stateData}">
<tr>
 <td class="label top">State Data</td>
 <td class="data"><c:forEach var="k" items="${fn:keys(stateData)}">
${k} = ${stateData[k]}<br /></c:forEach></td>
</tr>
</c:if>
<c:if test="${err.isLoaded()}">
<tr>
 <td class="label">Application Log</td>
 <td class="data"><span class="pri bld">acars_error${err.ID}.log</span> (<fmt:int value="${err.size / 1024}" />K) <a href="/error_log/${err.hexID}">Click to download</a></td>
</tr>
</c:if>
<c:if test="${!empty acarsClientInfo}">
<tr class="title caps">
 <td colspan="2">SYSTEM INFORMATION AS OF <fmt:date fmt="d" date="${acarsClientInfo.date}"  /></td>
</tr>
<tr>
 <td class="label">Operating System</td>
 <td class="data"><fmt:windows version="${acarsClientInfo.OSVersion}" /> (<fmt:int value="${acarsClientInfo.memorySize}" />KB memory) <span class="ita">as of <fmt:date fmt="d" date="${acarsClientInfo.date}" /></span></td>
</tr>
<tr>
 <td class="label">.NET Runtime</td>
 <td class="data"><span class="bld">${acarsClientInfo.dotNETVersion}</span> <span class="small">CLR: ${acarsClientInfo.CLRVersion}</span></td>
</tr>
<tr>
 <td class="label">Locale / Time Zone</td>
 <td class="data">${acarsClientInfo.locale} <span class="ita">Time Zone: ${acarsClientInfo.timeZone}</span></td>
</tr>
<tr>
 <td class="label">CPU Information</td>
 <td class="data">${acarsClientInfo.CPU}&nbsp;<span class="sec small ita">(<fmt:int value="${acarsClientInfo.cores}" /> cores, <fmt:int value="${acarsClientInfo.threads}" /> threads)</span></td>
</tr>
<tr>
 <td class="label">GPU Information</td>
 <td class="data">${acarsClientInfo.GPU}&nbsp;<span class="small ita">(<fmt:int value="${acarsClientInfo.videoMemorySize}" /> KB, ${acarsClientInfo.width}x${acarsClientInfo.height}x${acarsClientInfo.colorDepth}, ${acarsClientInfo.screenCount} screens)</span></td>
</tr>
<tr>
 <td class="label">GPU Driver</td>
 <td class="data">${acarsClientInfo.GPUDriverVersion}</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="acarserrordelete" link="${err}" label="DELETE ERROR REPORT" /></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
