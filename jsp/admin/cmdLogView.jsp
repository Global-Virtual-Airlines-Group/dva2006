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
<title><content:airline /> Command Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.addr, 8, 'IP address or host name')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="cmdlog.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> WEB SITE COMMAND LOG</td>
</tr>
<tr>
 <td class="label">IP Address / Host</td>
 <td class="data"><el:text name="addr" idx="*" size="48" max="96" value="${param.addr}" /></td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:text name="pilotName" idx="*" size="32" max="48" value="${pilot.name}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH LOG" /></td>
</tr>
</el:table>
</el:form>
<c:if test="${doSearch}">
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td colspan="x" class="left">COMMAND LOG RESULTS</td>
</tr>
<c:if test="${empty viewContext.results}">
<tr>
 <td colspan="x" class="pri bld">No Command Log entries matching your criteria were found.</td>
</tr>
</c:if>
<c:if test="${!empty viewContext.results}">
<tr class="title">
 <td width="15%">DATE</td>
 
</c:if>
</el:table>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
