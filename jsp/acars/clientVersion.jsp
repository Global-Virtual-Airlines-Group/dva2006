<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
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
if (!validateNumber(form.minBuild, 1, 'Minimum Build')) return false;
if (!validateNumber(form.latestBuild, parseInt(form.minBuild.value), 'Latest Build')) return false;

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
<content:sysdata var="minBuild" name="acars.build.minimum" />
<content:sysdata var="latestBuild" name="acars.build.latest" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsversion.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4"><content:airline /> ACARS CLIENT VERSION CONTROL</td>
</tr>
<tr>
 <td class="label">Minimum Build</td>
 <td class="data"><el:text className="pri bld req" name="minBuild" idx="*" size="3" max="4" value="${minBuild}" /></td>
</tr>
<tr>
 <td class="label">Latest Build</td>
 <td class="data"><el:text className="req" name="latestBuild" idx="*" size="3" max="4" value="${latestBuild}" /></td>
</tr>
<c:if test="${!empty system_message}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data error bld">${system_message}</td>
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
</body>
</html>
