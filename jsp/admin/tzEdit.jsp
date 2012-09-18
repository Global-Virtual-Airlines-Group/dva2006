<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<c:if test="${empty tz}">
<title>New <content:airline /> Time Zone</title>
</c:if>
<c:if test="${!empty tz}">
<title><content:airline /> Time Zone - ${tz.ID}</title>
</c:if>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 6, 'Time Zone Name')) return false;
if (!validateText(form.abbr, 2, 'Time Zone Code')) return false;
if (!validateCombo(form.newID, 'JVM Time Zone ID')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="tz.do" method="post" linkID="${isNew ? '' : tz.ID}" op="save" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">TIME ZONE PROFILE</td>
</tr>
<tr>
 <td class="label">Time Zone Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld req" size="36" max="48" value="${tz.name}" /></td>
</tr>
<tr>
 <td class="label">JVM Time Zone ID</td>
 <td class="data"><el:combo name="newID" className="req" idx="*" size="1" firstEntry="-" options="${tzIDs}" value="${tz.ID}" /></td>
</tr>
<tr>
 <td class="label">Time Zone Code</td>
 <td class="data"><el:text name="abbr" className="bld req" idx="*" size="4" max="4" value="${tz.abbr}" /></td>
</tr>
<tr class="title caps">
 <td colspan="2">WORLD TIME ZONE MAP</td>
</tr>
<tr>
 <td colspan="2" class="mid"><el:img src="worldzones.png" caption="Time Zone Map" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE TIME ZONE PROFILE" /></td>
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
