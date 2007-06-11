function getElement(eName)
{
return document.getElementById(eName);
}

function getElementsById(eName)
{
var elements = new Array();
var all = (typeof document.all != 'undefined') ? document.all : document.getElementsByTagName('*');
for (var x = 0; x < all.length; x++) {
	if (all[x].id == eName)
		elements.push(all[x]);
}

return elements;
}

function disableButton(btnName)
{
return enableElement(btnName, false);
}

function enableElement(eName, isEnabled)
{
var objs = getElementsById(eName);
if (!objs) return false;
for (var x = 0; x < objs.length; x++)
	enableObject(objs[x], isEnabled);

return true;
}

function enableObject(e, isEnabled)
{
if (e) e.disabled = (!isEnabled);
return true;
}

function checkSubmit()
{
if (document.isSubmit) {
	alert('This page is already being submitted. Please be patient.');
	return false;
}
 
return true;
}

function clearSubmit()
{
document.isSubmit = false;
return true;
}

function setSubmit()
{
document.isSubmit = true;
return true;
}

function cmdPost(url)
{
var form = document.forms[0];
form.action = url;
 
// Execute the form validation - if any
if (form.onsubmit) {
	submitOK = form.onsubmit();
	if (!submitOK)
		return false;
}
  
setSubmit();
form.submit();
return true;
}

function cmdGet(url)
{
setSubmit();
self.location = '/' + url;
return true;
}

function validateText(text, min, title)
{
if ((!text) || (text.disabled)) return true;
if (text.value.length < min) {
	alert('Please provide the ' + title + '.');
	text.focus();
	return false;
}

return true;
}

function validateNumber(text, minValue, title)
{
if ((!text) || (text.disabled)) return true;
intValue = parseFloat(text.value);
if ((text.value.length < 1) || (intValue == Number.NaN)) {
	alert('Please provide a numeric ' + title + '.');
	text.focus();
	return false;
}

if (intValue < minValue) {
	alert('The ' + title + ' must be greater than ' + minValue + '.');
	text.focus();
	return false;
}

return true;
}

function validateEMail(text, title)
{
if ((!text) || (text.disabled)) return true;

// Get the value
if (text.value.length < 5) {
	alert('Please provide a ' + title + '.');
	text.focus();
	return false;
}

// Test using regexp
var pattern = /^[\w](([_\.\-\+]?[\w]+)*)@([\w]+)(([\.-]?[\w]+)*)\.([A-Za-z]{2,})$/;
if (!pattern.test(text.value)) {
	alert('Please provide a valid ' + title + '.');
	text.focus();
	return false;
}

return true;
}

function validateCombo(combo, title)
{
if ((!combo) || (combo.disabled)) return true;
if (combo.selectedIndex == 0) {
	alert('Please provide the ' + title + '.');
	combo.focus();
	return false;
}

return true;
}

function validateFile(fileName, extType, title)
{
if ((!fileName) || (fileName.disabled) || (fileName.value.length == 0)) return true;
extTypes = extType.toLowerCase().split(',');

fName = fileName.value;
ext = fName.substring(fName.length - 3, fName.length).toLowerCase();
for (x = 0; x < extTypes.length; x++) {
	if (ext == extTypes[x])
		return true;
}

alert('The ' + title + ' must be a ' + extType.toUpperCase() + ' file.');
fileName.focus();
return false;
}

function validateCheckBox(checkbox, minSelected, title)
{
if ((!checkbox) || (!checkbox.length)) return true;
var checkCount = 0;
for (x = 0; x < checkbox.length; x++) {
	if (checkbox[x].checked)
		checkCount++;
}

if (checkCount >= minSelected) return true;
alert('At least ' + minSelected + ' ' + title + ' must be selected.');
checkbox[0].focus();
return false;
}

function setDaysInMonth(combo)
{
var daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
var month = parseInt(combo.options[combo.selectedIndex].value);

var dCombo = document.forms[0].dateD;
dCombo.options.length = daysInMonth[month];
for (x = 1; x <= daysInMonth[month]; x++)
	dCombo.options[x-1] = new Option(x);
	
return true;
}

function initDateCombos(mCombo, dCombo, d)
{
mCombo.selectedIndex = d.getMonth();
setDaysInMonth(mCombo);
dCombo.selectedIndex = (d.getDate() - 1);
return true;
}

function getXMLHttpRequest()
{
var req;
try {
	req = new XMLHttpRequest();
} catch (e) {
	var MSXML_XMLHTTP_PROGIDS = new Array('MSXML2.XMLHTTP.5.0','MSXML2.XMLHTTP.4.0','MSXML2.XMLHTTP.3.0','MSXML2.XMLHTTP','Microsoft.XMLHTTP');
	var success = false;
	for (var i = 0; i < MSXML_XMLHTTP_PROGIDS.length && (!req); i++) {
		try {
			req = new ActiveXObject(MSXML_XMLHTTP_PROGIDS[i]);
		} catch (e) {}
	}
}

return req;
}

function initLinks()
{
if (!document.getElementsByTagName) return false;
var anchors = document.getElementsByTagName("a");
for (var i = 0; i < anchors.length; i++) {
	var anchor = anchors[i];
	if (anchor.getAttribute("href") && anchor.getAttribute("rel") == "external")
		anchor.target = "_blank";
}

return true;
}

function showHelp(helpname)
{
URLflags = 'height=460,width=660,menubar=no,toolbar=no,status=no,scrollbars=yes,resizable=yes';
var w = window.open('/help.do?id=' + helpname, 'HELP', URLflags);
return true;
}
