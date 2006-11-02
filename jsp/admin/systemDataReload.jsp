<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> System Data Reload</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!confirm("Are you sure you wish to continue?")) return false;

setSubmit();
disableButton('ReloadButton');
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
<el:form method="post" action="sysdatareload.do" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">SYSTEM CONFIGURATION DATA RELOAD</td>
</tr>
<tr>
 <td colspan="2" class="pri bld">Reloading System Configuration data is a significant operation that
 may adversely affect the behavior of the web application. You should not execute this Command unless
 you have made changes to systemConfig.xml, or you have made changes to the list of Airports in another
 web application running on this server, and need to resync all running applications.</td>
</tr>
<c:if test="${isReload}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><div class="error bld">System Configuration Data reloaded successfully!</div>
<br />
SystemData loader class - <b>${loader}</b>
<c:if test="${isSchedReload}"><br />
Schedule Database Reloaded</c:if></td>
</tr>
</c:if>
<tr>
 <td class="label">Options</td>
 <td class="data"><el:box name="reloadSchedule" idx="*" value="true" label="Reload Schedule Tables" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="ReloadButton" type="SUBMIT" className="BUTTON" label="RELOAD SYSTEM CONFIGURATION" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
