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
<script type="text/javascript">
function selectAC(combo)
{
if (combo.selectedIndex < 1) {
	document.fName = null;
	showObject(getElement('installerInfo'), false);
	return false;
}

// Get the code
var code = combo.options[combo.selectedIndex].value;
var xmlreq = getXMLHttpRequest();
xmlreq.open('GET', 'fleetlib.ws?code=' + code, true);
xmlreq.onreadystatechange = function() {
	if (xmlreq.readyState != 4) return false;
	var xmlDoc = xmlreq.responseXML;
	var infoElements = xmlDoc.documentElement.getElementsByTagName("installer");
	var info = infoElements[0];

	// Get the Flight Simulator versions
	var verDesc = 'This <content:airline /> Fleet Library installer is compatible with ';
	var versions = info.getElementsByTagName('version');
	for (var x = 0; x < versions.length; x++) {
		var vE = versions[x];
		verDesc = verDesc + ((vE.text) ? vE.text : vE.textContent);
		if (x < (versions.length - 1))
			verDesc = verDesc + ', ';
	}

	// Update the page
	document.fName = info.getAttribute('filename');
	getElement('FleetPic').src = info.getAttribute('img');
	getElement('divName').innerHTML = info.getAttribute('title');
	var dt = info.getAttribute('date');
	if (dt)
		getElement('divDT').innerHTML = dt;
	getElement('divSize').innerHTML = info.getAttribute('size');
	getElement('FSVersions').innerHTML = (versions.length == 0) ? '' : (verDesc + '.');

	// Load the description
	var descE = info.getElementsByTagName('desc')[0].firstChild;
	getElement('divDesc').innerHTML = descE.data;
	combo.disabled = false;
	showObject(getElement('installerInfo'), true);
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
<el:table className="form">
<tr class="title">
 <td class="caps"><content:airline /> FLEET LIBRARY</td>
 <td class="right">SELECT <el:combo name="instName" idx="1" size="1" firstEntry="< INSTALLER >" options="${fleet}" onChange="void selectAC(this)" /></td>
</tr>
<tr>
 <td colspan="2"><el:img ID="FleetPic" style="float:left; margin:4px;" x="164" y="314" src="blank.png" caption="Fleet Library" />
 <div id="installerInfo" class="top" style="visibility:hidden; margin:4px;"><span id="divName" class="pri bld"></span><br /><br />
<span class="sec bld"><span id="divSize"></span>&nbsp;bytes, last modified on <span id="divDT"></span></span><br />
<span id="FSVersions" class="pri bld small"></span><br /><br />
<span id="divDesc">The <content:airline /> Fleet Library contains Windows installation packages to let 
you quickly and easily install all aircraft in our fleet, and the fleets of our partner airlines. Each 
aircraft comes in a number of liveries, along with a high quality freeware panel and the ability to 
download a sound package and an operating manual from the <content:airline /> Document Library.<br />
<br />
Select a Fleet Installer from the list above.</span>
</div></td>
</tr>
</el:table>

<!-- Download Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="DownloadButton" className="BUTTON" label="DOWNLOAD INSTALLER" onClick="void download()" /></td>
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
