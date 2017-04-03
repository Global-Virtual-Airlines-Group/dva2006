<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Installer Log</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
var isOK = golgotha.form.comboSet(f.installerCode) || golgotha.form.comboSet(f.os);
if (!isOK && (f.userCode.value.length < 2)) {
	alert('Please provide an Installer Code, Operating System or User Code.');
	f.installerCode.focus();
	return false;
}

golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="fleetlog.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">BROWSE INSTALLER SYSTEM DATA</td>
</tr>
<tr>
 <td class="label">Installer Code</td>
 <td class="data"><el:combo name="installerCode" idx="*" size="1" firstEntry="-" options="${installers}" value="${param.installerCode}" /></td>
</tr>
<tr>
 <td class="label">Operating System</td>
 <td class="data"><el:combo name="os" idx="*" size="1" firstEntry="-" options="${osList}" value="${param.os}" /></td>
</tr>
<tr>
 <td class="label">User Code</td>
 <td class="data"><el:text name="userCode" idx="*" size="12" max="18" value="${param.userCode}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SearchButton" type="submit" label="SEARCH FLEET INSTALLER DATA" /></td>
</tr>
</el:table>
</el:form>

<c:if test="${!empty viewContext.results}">
<view:table cmd="fleetlog">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:10%">DATE</td>
 <td style="width:10%">INSTALLER</td>
 <td style="width:10%">OS</td>
 <td style="width:25%">CPU</td>
 <td style="width:25%">GPU</td> 
 <td style="width:10%">RAM</td>
 <td>DIRECTX</td>
</tr>

<!-- Table Log Data -->
<c:forEach var="entry" items="${viewContext.results}">
<view:row entry="${entry}">
 <td class="sec bld"><fmt:date date="${entry.date}" fmt="d" /></td>
 <td class="pri bld">${entry.code}</td>
 <td>${entry.OS}</td>
 <td class="small">${entry.CPU}</td>
 <td class="small">${entry.GPU}</td>
 <td class="pri">${entry.RAM} MB</td>
 <td>${entry.directX}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</c:if>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
