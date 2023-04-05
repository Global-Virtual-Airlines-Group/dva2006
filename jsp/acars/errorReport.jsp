<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Client Error Report</title>
<content:expire expires="30" />
<content:css name="main" />
<content:css name="form" />
<content:js name="fileSaver" />
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
<el:form method="get" action="acarserror.do" validate="return false">
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
<c:if test="${!empty err.pluginVersion}">
<tr>
 <td class="label">Simulator Plugin</td>
 <td class="data">${err.pluginVersion}</td>
</tr>
</c:if>
<c:if test="${!empty err.bridgeVersion}">
<tr>
 <td class="label">Simulator Bridge</td>
 <td class="data">${err.bridgeVersion}</td>
</tr>
</c:if>
<tr>
 <td class="label">Reported from</td>
 <td class="data"><fmt:ipaddr addr="${err}" info="${ipInfo}" showFlag="true" /></td>
</tr>
<c:if test="${!empty err.stackDump && !err.isInfo}">
<tr>
 <td class="label top">Error Information</td>
 <td class="data"><fmt:text value="${err.stackDump}" /></td>
</tr>
</c:if>
<c:if test="${!empty stateData}">
<tr>
 <td class="label top">State Data</td>
 <td class="data"><c:forEach var="k" items="${stateData.keySet()}">
${k} = ${stateData[k]}<br /></c:forEach></td>
</tr>
</c:if>
<c:choose>
<c:when test="${err.isLoaded() && !err.isInfo}">
<tr>
 <td class="label">Application Log</td>
 <td class="data"><span class="pri bld">acars_error${err.ID}.log</span> (<fmt:fileSize value="${err.size}" />) <a href="/error_log/${err.hexID}">Click to download</a></td>
</tr>
</c:when>
<c:when test="${err.isLoaded()}">
<tr>
 <td class="label top">Application Log</td>
 <td id="logData" class="data small"><a href="javascript:golgotha.local.loadLog('${err.hexID}')">SHOW LOG DATA</a> <el:box name="saveLog" value="true" checked="false" label="Save Log file locally" /></td>
</tr>
</c:when>
</c:choose>
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
 <td class="data">${acarsClientInfo.locale}&nbsp;<span class="ita">Time Zone: ${acarsClientInfo.timeZone}</span></td>
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
 <td>&nbsp;<c:if test="${access.canDelete}"><el:cmdbutton url="acarserrordelete" link="${err}" label="DELETE ERROR REPORT" /></c:if></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
<script async>
golgotha.local.loadLog = function(id) {
	const doSave = document.forms[0].saveLog.checked;
	const xmlreq = new XMLHttpRequest();
	xmlreq.open('get', '/attach/error_log/' + id, true);
	xmlreq.onreadystatechange = function() {
		if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
		const td = document.getElementById('logData');
		td.innerText = xmlreq.responseText;
		if (doSave) {
			const ct = xmlreq.getResponseHeader('Content-Type');
			const b = new Blob([xmlreq.response], {type: ct.substring(0, ct.indexOf(';')), endings:'native'});
			const decID = parseInt(id.substring(2), 16);
			saveAs(b, 'acarsError_' + decID + '.log');
		}

		return true;
	};

	xmlreq.send(null);
	return true;
};
</script>
</html>
