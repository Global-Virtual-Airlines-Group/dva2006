<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Account Suspension</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
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
<el:table className="form" space="default" pad="default">
<tr class="title">
 <td colspan="2" class="left caps">NEW STATUS COMMENT FOR ${pilot.name}</td>
</tr>
<tr>
 <td colspan="2" class="pri bld mid">Suspending a Pilot will remove his or her ability to log into the
 web site<c:if test="${acarsEnabled}"> and ACARS</c:if>, and will automatically disconnect the Pilot if
 already authenticated.</td>
</tr>
<tr>
 <td class="label" valign="top">Reason for Suspension</td>
 <td class="data"><el:textbox name="comment" idx="*" width="80%" height="5" className="req" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SUSPEND USER" /></td>
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
