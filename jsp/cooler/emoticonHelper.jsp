<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title>Emoticons</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:enum var="icons" className="org.deltava.beans.cooler.Emoticons" />
<script>
golgotha = {local:{}};
golgotha.local.addEmoticon = function(name)
{
// Get the window opener
const parent = window.opener;
if (!parent) {
	alert('Your ${forumName} post does not appear to be open.');
	self.close();
	return false;
}

// Get the form
const f = parent.document.forms[0];
if ((!f) || (!f.msgText)) return false;

// Append the emoticon
f.msgText.value = f.msgText.value + ' :' + name + ':';
self.close();
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void window.focus()">
<el:table className="form">
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
<c:forEach var="icon" items="${icons}">
<c:set var="iconCode" value="${iconCodes[icon.name]}" scope="page" />
<tr class="mid">
 <td><a href="javascript:void golgotha.local.addEmoticon('${icon.name}')"><el:img className="noborder" src="cooler/emoticons/${icon.name}.gif" caption="${icon.name}" /></a></td>
 <td class="bld">:${icon.name}:</td>
 <td class="sec bld">${empty iconCode ? '&nbsp;' : iconCode}</td>
</tr>
</c:forEach>

<!-- Button Bar -->
<tr class="title mid">
 <td colspan="3"><el:button onClick="void self.close()" label="CLOSE WINDOW" /></td>
</tr>
</el:table>
<content:googleAnalytics />
</body>
</html>
