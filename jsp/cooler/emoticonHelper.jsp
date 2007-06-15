<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Water Cooler Emoticons</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function addEmoticon(name)
{
// Get the window opener
var parent = window.opener;
if (!parent) {
	alert('Your Water Cooler post does not appear to be open.');
	self.close();
	return false;
}

// Get the form
var f = parent.document.forms[0];
if (!f) return false;
var msgText = f.msgText;
if (!msgText) return false;

// Append the emoticon
msgText.value = msgText.value + ' :' + name + ':';
self.close();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void window.focus()">
<el:table className="form" space="default" pad="default">
<!-- Table Header Bars -->
<tr class="title caps">
 <td class="mid">EMOTICON</td>
 <td class="mid">CODE</td>
 <td class="mid">SYMBOL</td>
</tr>
<tr class="small mid">
 <td colspan="3">Click on an emoticon to add it to the end of your post.</td>
</tr>

<!-- Table Emoticons -->
<c:forEach var="iconName" items="${iconNames}">
<c:set var="iconCode" value="${iconCodes[iconName]}" scope="request" />
<tr class="mid">
 <td><a href="javascript:addEmoticon('${iconName}')"><el:img border="0" src="cooler/emoticons/${iconName}.gif" caption="${iconName}" /></a></td>
 <td class="bld">:${iconName}:</td>
 <td class="sec bld">${empty iconCode ? '&nbsp;' : iconCode}</td>
</tr>
</c:forEach>

<!-- Button Bar -->
<tr class="title mid">
 <td colspan="3"><el:button className="BUTTON" onClick="void self.close()" label="CLOSE WINDOW" /></td>
</tr>
</el:table>
<content:googleAnalytics />
</body>
</html>
