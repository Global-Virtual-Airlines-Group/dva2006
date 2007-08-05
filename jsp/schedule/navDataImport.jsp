<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Navigation Data Import</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
var dataFiles = ['pssapt.dat','pssndb.dat','pssrwy.dat','pssvor.dat','psswpt.dat','pssawy.dat','pssstar.dat','psssid.dat'];

function validate(form)
{
if (!checkSubmit()) return false;

isOK = false;
fName = form.navData.value.substring(form.navData.value.lastIndexOf('\\') + 1);
for (x = 0; x < dataFiles.length && !isOK; x++)
	isOK = isOK || (fName == dataFiles[x]);
	
if (!isOK) {
	alert('This does not appear to be a valid PSS AIRAC data file.');
	form.navData.focus();
	return false;
}

setSubmit();
disableButton('SaveButton');
disableButton('PurgeButton');
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
<el:form action="navimport.do" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">PSS AIRAC NAVIGATION DATA UPLOAD</td>
</tr>
<tr>
 <td class="label" valign="top">Upload Data File</td>
 <td class="data"><el:file name="navData" idx="*" className="small req" size="80" max="144" />&nbsp;
<span class="small">AIRAC data must be in PSS Format.</span></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="UPLOAD AIRAC NAVIGATION DATA" />&nbsp;
 <el:cmdbutton ID="PurgeButton" url="navpurge" label="PURGE NAVIGATION DATA" /></td>
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
