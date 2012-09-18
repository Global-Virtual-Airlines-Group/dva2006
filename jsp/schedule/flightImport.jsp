<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule Import</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.schedType, 'Schedule Type')) return false;
if (!validateFile(form.csvData, 'csv', 'Flight Schedule data')) return false;

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
<el:form action="schedimport.do" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FLIGHT SCHEDULE DATA UPLOAD - STEP ONE</td>
</tr>
<tr>
 <td class="label top">Upload Data File</td>
 <td class="data"><el:file name="csvData" idx="*" className="small req" size="80" max="144" /></td>
</tr>
<tr>
 <td class="label">Schedule Format</td>
 <td class="data"><el:combo name="schedType" idx="*" size="1" options="${schedTypes}" firstEntry="-" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPLOAD FLIGHT SCHEDULE DATA" /></td>
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
