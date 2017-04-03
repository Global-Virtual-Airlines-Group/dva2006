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
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.dataFiles = ['geolitecityv6.csv'];
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;

var isOK = false;
var fName = f.netblockData.value.substring(f.netblockData.value.lastIndexOf('\\') + 1).toLowerCase();
for (x = 0; x < golgotha.local.dataFiles.length && !isOK; x++)
	isOK = isOK || (fName == golgotha.local.dataFiles[x]) || (fName == (golgotha.local.dataFiles[x] + '.gz'));

if (!isOK)
	throw new golgotha.event.ValidationError('This does not appear to be a valid GeoLite IPv6 City data file.', f.netblockData);

golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="ip6import.do" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
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
