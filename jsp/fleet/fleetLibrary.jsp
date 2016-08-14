<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Fleet Library</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:sysdata var="imgPath" name="path.img" />
<script type="text/javascript">
golgotha.local.selectAC = function(combo)
{
if (!golgotha.form.comboSet(combo)) {
	golgotha.local.fName = null;
	golgotha.util.show('installerInfo', false);
	return false;
}

// Get the code
var xmlreq = new XMLHttpRequest();
xmlreq.open('GET', 'fleetlib.ws?code=' + escape(golgotha.form.getCombo(combo)), true);
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
	golgotha.local.fName = info.getAttribute('filename');
	document.getElementById('FleetPic').src = info.getAttribute('img');
	document.getElementById('divName').innerHTML = info.getAttribute('title');
	var dt = info.getAttribute('date');
	if (dt)
		document.getElementById('divDT').innerHTML = dt;
	document.getElementById('divSize').innerHTML = info.getAttribute('size');
	document.getElementById('FSVersions').innerHTML = (versions.length == 0) ? '' : (verDesc + '.');

	// Load the description
	var descE = info.getElementsByTagName('desc')[0].firstChild;
	document.getElementById('divDesc').innerHTML = descE.data;
	combo.disabled = false;
	golgotha.util.show('installerInfo', true);
	return true;
};

combo.disabled = true;
xmlreq.send(null);
return true;
};

golgotha.local.download = function() {
	if (!golgotha.local.fName) return false;
	self.location = '/fleet/' + golgotha.local.fName;
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
<el:form action="fleetlibrary.do" method="get" validate="return false">
<el:table className="form">
<tr class="title">
 <td class="caps"><span class="nophone"><content:airline /> </span>FLEET LIBRARY</td>
 <td class="right"><span class="nophone">SELECT </span><el:combo name="instName" idx="1" size="1" firstEntry="[ INSTALLER ]" options="${fleet}" onChange="void golgotha.local.selectAC(this)" /></td>
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
Please select a <content:airline /> Fleet Installer from the list above.</span>
</div></td>
</tr>
</el:table>

<!-- Download Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="DownloadButton" label="DOWNLOAD INSTALLER" onClick="void golgotha.local.download()" /></td>
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
