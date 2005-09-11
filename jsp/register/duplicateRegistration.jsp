<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Duplicate Registration Detected</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.msgtohr, 10, 'Message to Human Resources')) return false;

setSubmit();
disableButton('SubmitButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="senddupeinfo.do" method="post" validate="return validate(this)">
<div class="updateHdr">Duplicate Registration Detected</div>
<br />
There is another pilot registered in our database with your provided name and/or e-mail address. If you are an inactive pilot wishing to return to active status at <content:airline />, please fill in your details in the form below, which will be sent to our Human Resources department. You should hear back from them within 48-72 hours. Thank you for your interest in <content:airline />!<br />
<br />
<span class="pri bld caps">MESSAGE TO HUMAN RESOURCES:</span> <el:textbox name="msgtohr" idx="*" width="150" height="7" /></el:textbox><br />
<br />
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SubmitButton" type="submit" className="BUTTON" label="SUBMIT MESSAGE" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
