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
<title><content:airline /> Installer Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
isOK = (form.installerCode.selectedIndex > 0);
osOK = (form.os.selectedIndex > 0);

if (!isOK && !osOK && (form.userCode.value.length < 2)) {
	alert('Please provide an Installer Code, Operating System or User Code.');
	form.installerCode.focus();
	return false;
}

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
<el:form action="fleetlog.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
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
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH FLEET INSTLALER DATA" /></td>
</tr>
</el:table>
</el:form>

<c:if test="${!empty viewContext.results}">
<view:table className="view" pad="default" space="default" cmd="fleetlog">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">DATE</td>
 <td width="10%">INSTALLER</td>
 <td width="10%">OS</td>
 <td width="25%">CPU</td>
 <td width="25%">GPU</td> 
 <td width="10%">RAM</td>
 <td>DIRECTX</td>
</tr>

<!-- Table Log Data -->
<c:forEach var="entry" items="${viewContext.results}">
<view:row entry="${entry}">
 <td class="sec bld"><fmt:date date="${entry.date}" /></td>
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
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</c:if>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
