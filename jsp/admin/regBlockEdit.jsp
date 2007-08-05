<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><c:if test="${empty block}">New </c:if><content:airline /> Registration Block Entry</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
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
<el:form method="post" action="regblock.do" link="${block}" op="save" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2">REGISTRATION BLACKLIST ENTRY</td>
</tr>
<tr>
 <td class="label">First / Last Name</td>
 <td class="data"><el:text name="firstName" idx="*" size="16" max="32" value="${block.firstName}" />
 <el:text name="lastName" idx="*" size="16" max="32" value="${block.lastName}" /></td>
</tr>
<tr>
 <td class="label">Host Name</td>
 <td class="data"><el:text name="hostName" idx="*" size="48" max="128" value="${block.hostName}" /></td>
</tr>
<tr>
 <td class="label">IP Address / Mask</td>
 <td class="data"><el:text name="addr" idx="*" size="12" max="15" value="${addr}" />
 <el:text name="netMask" idx="*" size="12" max="15" value="${mask}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="hasFeedback" idx="*" value="true" checked="${block.hasUserFeedback}" label="Provide Feedback on Rejection" /><br />
<el:box name="active" idx="*" value="true" checked="${block.active}" label="Registration Block entry is Active" /></td>
</tr>
<tr>
 <td class="label" valign="top">Comments</td>
 <td class="data"><el:textbox name="comments" idx="*" width="80%" height="4">${block.comments}</el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE ENTRY" />
&nbsp;<el:cmdbutton ID="DeleteButton" url="regblockdelete" link="${block}" label="DELETE ENTRY" /></td>
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
