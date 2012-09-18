<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Airway Data Import</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
var dataFiles = ['pssawy.dat'];
function validate(form)
{
if (!checkSubmit()) return false;

var isOK = false;
fName = form.navData.value.substring(form.navData.value.lastIndexOf('\\') + 1).toLowerCase();
for (x = 0; x < dataFiles.length && !isOK; x++)
	isOK = isOK || (fName == dataFiles[x]) || (fName == (dataFiles[x] + '.gz'));
	
if (!isOK) {
	alert('This does not appear to be a valid PSS AIRAC data file.');
	form.navData.focus();
	return false;
}

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
<el:form action="awyimport.do" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">PSS AIRAC NAVIGATION DATA UPLOAD</td>
</tr>
<tr>
 <td class="label">Upload Data File</td>
 <td class="data"><el:file name="navData" idx="*" className="small req" size="80" max="144" />&nbsp;
<span class="small">AIRAC data must be in PSS Format.</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="doPurge" idx="*" className="small" value="true" checked="true" label="Purge Airway Data before import" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPLOAD AIRAC AIRWAY DATA" /></td>
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
