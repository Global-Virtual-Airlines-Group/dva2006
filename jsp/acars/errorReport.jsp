<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Client Error Report</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
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
 <td class="data">${err.remoteAddr} (${err.remoteHost}) <c:if test="${!empty ipInfo}"> <el:flag countryCode="${ipInfo.country.code}" caption="${ipInfo.location}" />
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
 <td class="data">
<c:forEach var="k" items="${fn:keys(stateData)}">
${k} = ${stateData[k]}<br />
</c:forEach></td>
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
