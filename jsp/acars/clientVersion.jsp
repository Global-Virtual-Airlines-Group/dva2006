<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Client Version Control</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateNumber(form.latestBuild, 1, 'Latest Build')) return false;
if (!validateNumber(form.latestDispatch, 1, 'Latest Dispatch Build')) return false;
<c:forEach var="ver" items="${fn:keys(versionInfo)}">
<c:set var="versionCode" value="${fn:replace(ver, '.', '_')}" scope="page" />
if (!validateNumber(form.min_${versionCode}_Build, 1, 'Minimum ${ver} Build')) return false;
</c:forEach>
<c:forEach var="build" items="${fn:keys(betaInfo)}">
if (!validateNumber(form.min_${build}_beta, 0, 'Minimum Build ${build} beta version')) return false;
</c:forEach>
setSubmit();
disableButton('SaveButton');
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
<el:form action="acarsversion.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">GLOBAL ACARS CLIENT VERSION CONTROL</td>
</tr>
<c:forEach var="ver" items="${fn:keys(versionInfo)}">
<c:set var="versionCode" value="${fn:replace(ver, '.', '_')}" scope="page" />
<c:set var="minBuild" value="${versionInfo[ver]}" scope="page" />
<tr>
 <td class="label">Minimum ${ver} Build</td>
 <td class="data"><el:text className="pri bld req" name="min_${versionCode}_build" idx="*" size="3" max="4" value="${minBuild}" /></td>
</tr>
</c:forEach>
<tr>
 <td class="label">Latest Build</td>
 <td class="data"><el:text className="req" name="latestBuild" idx="*" size="3" max="4" value="${latestBuild}" /></td>
</tr>
<tr>
 <td class="label">Minimum Dispatch Build</td>
 <td class="data"><el:text className="req" name="latestDispatch" idx="*" size="3" max="4" value="${latestDispatch}" /></td>
</tr>
<tr>
 <td class="label">Client Builds without Dispatch</td>
 <td class="data"><el:text name="noDispatch" idx="*" size="12" max="24" value="${fn:splice(noDispatch, ',')}" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">ACARS BETA VERSION CONTROL</td>
</tr>
<c:if test="${!empty betaInfo}">
<c:forEach var="build" items="${fn:keys(betaInfo)}">
<c:set var="minBeta" value="${betaInfo[build]}" scope="page" />
<tr>
 <td class="label">Minimum Build ${build} beta</td>
 <td class="data"><el:text className="pri bld req" name="min_${build}_beta" idx="*" size="3" max="4" value="${minBeta}" /></td>
</tr>
</c:forEach>
<tr>
 <td class="label">New Beta</td>
 <td class="data">Build <el:text name="newBuild" size="3" max="4" idx="*" value="" /> Beta
 <el:text name="newBeta" size="3" max="4" idx="*" value="1" /></td>
</tr>
</c:if>

<!-- Button Bar -->
<tr class="title">
 <td colspan="2" class="mid"><el:button ID="SaveButton" type="submit" className="BUTTON" label="UPDATE VERSIONS" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
