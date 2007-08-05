<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Fleet Library</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:sysdata var="imgPath" name="path.img" />
<script language="JavaScript" type="text/javascript">
function selectAC(combo)
{
var idx = combo.selectedIndex - 1;
if (idx < 0) {
	document.fName = null;
	return false;
}

// Get the code
var code = combo.options[combo.selectedIndex].value;
var xmlreq = getXMLHttpRequest();
xmlreq.open("GET", "fleetlib.ws?code=" + code, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var xmlDoc = xmlreq.responseXML;
	var infoElements = xmlDoc.documentElement.getElementsByTagName("installer");
	var info = infoElements[0];
	
	// Update the page
	document.fName = info.getAttribute('filename');
	getElement('FleetPic').src = info.getAttribute('img');
	getElement('divName').innerHTML = info.getAttribute('title');
	getElement('divSize').innerHTML = info.getAttribute('size') + ' bytes';

	// Load the description
	var descE = info.firstChild;
	getElement('divDesc').innerHTML = descE.data;

	combo.disabled = false;
	return true;
}

combo.disabled = true;
xmlreq.send(null);
return true;
}

function download()
{
// Check if an installer is selected
if (!document.fName)
	return false;

self.location = '/fleet/' + document.fName;
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
<el:form action="fleetlibrary.do" method="get" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td class="caps">FLEET LIBRARY</td>
 <td class="right">SELECT <el:combo name="instName" idx="1" size="1" firstEntry="< INSTALLER >" options="${fleet}" onChange="void selectAC(this)" /></td>
</tr>
<tr>
 <td class="fleetImg" rowspan="2"><el:img ID="FleetPic" x="164" y="314" src="blank.png" caption="Fleet Library" /></td>
 <td valign="top"><div id="divName" class="pri bld"></div><br />
<div id="divSize" class="sec bld"></div><br />
<div id="divDesc">The <content:airline /> Fleet Library contains Windows installation packages to let 
you quickly and easily install all aircraft in our fleet, and the fleets of our partner airlines. Each 
aircraft comes in a number of liveries, along with a high quality freeware panel and the ability to 
download a sound package and an operating manual from the <content:airline /> Document Library.<br />
<br />
Select a Fleet Installer from the list above.</div>
</td>
</tr>
</el:table>

<!-- Download Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="DownloadButton" className="BUTTON" label="DOWNLOAD INSTALLER" onClick="void download(document.fileName)" /></td>
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
