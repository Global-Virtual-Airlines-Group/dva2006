<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Account Suspension</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.comment, 6, 'Reason for Suspension')) return false;

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
<content:sysdata var="acarsEnabled" name="acars.enabled" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="suspend.do" link="${pilot}" validate="return validate(this)">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="left caps">NEW STATUS COMMENT FOR ${pilot.name}</td>
</tr>
<tr>
 <td colspan="2" class="pri bld mid">Suspending a Pilot will remove his or her ability to log into the
 <content:airline /> web site<c:if test="${acarsEnabled}"> and ACARS</c:if>, and will automatically
 disconnect the Pilot if  already authenticated.</td>
</tr>
<tr>
 <td class="label top">Reason for Suspension</td>
 <td class="data"><el:textbox name="comment" idx="*" width="90%" height="4" className="req" resize="true" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SUSPEND USER" /></td>
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
