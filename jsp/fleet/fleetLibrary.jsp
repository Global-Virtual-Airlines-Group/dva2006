<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
if (idx < 0)
	return false;

getElement('FleetPic').src = '/${imgPath}/fleet/' + fImgs[idx];
getElement('divName').innerHTML = iName[idx];
getElement('divDesc').innerHTML = fDesc[idx];
getElement('divSize').innerHTML = fSize[idx];
document.fileName = fName[idx];
return true;
}

function download(fileName)
{
self.location = '/library/' + fileName;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td class="caps">FLEET LIBRARY</td>
 <td class="right">SELECT <el:combo name="instName" idx="1" size="1" firstEntry="< INSTALLER >" options="${fleet}" onChange="void selectAC(this)" /></td>
</tr>
<tr>
 <td class="fleetImg" rowspan="2"><el:img ID="FleetPic" x="164" y="314" src="blank.png" /></td>
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
<content:copyright />
</div>
<script language="JavaScript" type="text/javascript">
var iName = new Array();
var fName = new Array();
var fSize = new Array();
var fDesc = new Array();
var fImgs = new Array();

<c:forEach var="entry" items="${fleet}">
iName.push('${entry.name}');
fName.push('${entry.fileName}');
fSize.push('<fmt:int value="${entry.size}" /> bytes');
fDesc.push("${fn:escape(entry.description)}");
fImgs.push('${entry.image}');
</c:forEach>
</script>
</body>
</html>
