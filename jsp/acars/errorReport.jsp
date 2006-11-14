<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Client Error Report</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
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
 <td colspan="2">ACARS CLIENT ERROR REPORT INFORMATION - ERROR #<fmt:int value="${err.ID}" /></td>
</tr>
<tr>
 <td class="label">Error Message</td>
 <td class="data pri bld">${err.message}</td>
</tr>
<tr>
 <td class="label">Logged on</td>
 <td class="data"><b><fmt:date date="${err.createdOn}" /></b> by ${author.name}</td>
</tr>
<tr>
 <td class="label">Client Build</td>
 <td class="data">Build <fmt:int value="${err.clientBuild}" /></td>
</tr>
<c:if test="${err.FSVersion > 0}">
<tr>
 <td class="label">Flight Simulator</td>
 <td class="data bld">Flight Simulator <fmt:int value="${err.FSVersion}" /></td>
</tr>
<tr>
 <td class="label">FSUIPC Version</td>
 <td class="data">${err.FSUIPCVersion}</td>
</tr>
</c:if>
<c:if test="${err.FSVersion == 0}">
<tr>
 <td class="label">Flight Simulator</td>
 <td class="data bld">NOT RUNNING AT TIME OF ERROR</td>
</tr>
</c:if>
<tr>
 <td class="label">Reported from</td>
 <td class="data">${err.remoteAddr} (${err.remoteHost})</td>
</tr>
<c:if test="${!empty err.stackDump}">
<tr>
 <td class="label" valign="top">Error Information</td>
 <td class="data"><fmt:text value="${err.stackDump}" /></td>
</tr>
</c:if>
<c:if test="${!empty err.stateData}">
<tr>
 <td class="label" valign="top">State Data</td>
 <td class="data">${err.stateData}</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:cmdbutton url="acarserrordelete" linkID="0x${err.ID}" label="DELETE ERROR REPORT" /></td>
</tr>
</el:table>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
