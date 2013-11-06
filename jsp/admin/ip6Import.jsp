<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> IPv6 Network Block Import</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
var dataFiles = ['geolitecityv6.csv'];
function validate(form)
{
if (!checkSubmit()) return false;

var isOK = false;
fName = form.netblockData.value.substring(form.netblockData.value.lastIndexOf('\\') + 1).toLowerCase();
for (x = 0; x < dataFiles.length && !isOK; x++)
	isOK = isOK || (fName == dataFiles[x]) || (fName == (dataFiles[x] + '.gz')) || (fName == (dataFiles[x] + '.bz2'));

if (!isOK) {
	alert('This does not appear to be a valid GeoLite IPv6 City data file.');
	form.netblockData.focus();
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
<el:form action="ip6import.do" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">GEOLITE IPV6 CITY NETWORK BLOCK UPLOAD</td>
</tr>
<tr>
 <td class="label">Upload Data File</td>
 <td class="data"><el:file name="netblockData" idx="*" className="small req" size="80" max="144" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPLOAD IPV6 NETWORK BLOCK DATA" /></td>
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
