function getElement(eName)
{
return document.getElementById(eName);
}

function getElementsById(id, eName)
{
var elements = [];
var all = document.getElementsByTagName((eName == null) ? '*' : eName);
for (var x = 0; x < all.length; x++) {
	if (all[x].id == id)
		elements.push(all[x]);
}

return elements;
}

function getElementsByClass(cName, eName)
{
var elements = [];
var all = document.getElementsByTagName((eName == null) ? '*' : eName);
for (var x = 0; x < all.length; x++) {
	var c = all[x].className;
	if (c.indexOf) {
		if ((c.indexOf(' ' + cName) > -1) || (c.indexOf(cName + ' ') > -1) || (c == cName)) {
			elements.push(all[x]);
		}
	}
}

return elements;
}

function enableObject(e, isEnabled)
{
if (e) e.disabled = (!isEnabled);
return true;
}

function enableElement(eName, isEnabled)
{
var objs = getElementsById(eName);
if (!objs) return false;
for (var x = 0; x < objs.length; x++) {
	enableObject(objs[x], isEnabled);
}

return true;
}

function disableButton(btnName)
{
return enableElement(btnName, false);
}

function showObject(e, isVisible)
{
if (e) e.style.visibility = isVisible ? 'visible' : 'hidden';
return true;
}

function displayObject(e, isVisible)
{
if (e) e.style.display = isVisible ? '' : 'none';
return true;
}

function resizeAll()
{
var boxes = getElementsByClass('resizable');
for (var x = 0; x < boxes.length; x++)
	resize(boxes[x]);

return true;
}

function resize(textbox)
{
if (!textbox) return false;
if (!textbox.minRows) {
	textbox.minRows = textbox.rows;
	textbox.minCols = Math.max(textbox.cols, 80);
}

var data = textbox.value.split('\n');
var lines = data.length;
for (var x = 0; x < data.length; x++) {
    if ((textbox.cols > 0) && (data[x].length >= textbox.minCols))
        lines += Math.floor(data[x].length / textbox.minCols);
}

textbox.rows = Math.max(textbox.minRows, lines);
return true;
}

function setCombo(combo, entryValue)
{
if (!combo) return false;
for (var x = 0; x < combo.options.length; x++) {
	var opt = combo.options[x];
	if ((opt.value == entryValue) || (opt.text == entryValue)) {
		combo.selectedIndex = x;
		return true;
	}
}

combo.selectedIndex = -1;
return false;
}

function getChild(e, name)
{
var children = e.getElementsByTagName(name);
return (children.length == 0) ? null : children[0];
}

if (window.Element != undefined)
	Element.prototype.getChild = function(name) { return getChild(this, name); };

getCDATA = function(e)
{
var child = e.firstChild;	
while ((child != null) && (child.nodeType != 4))
	child = child.nextSibling;

return child;
};

if (window.Element != undefined)
	Element.prototype.getCDATA = function() { return getCDATA(this); };

Array.prototype.remove = function(obj)
{
for (var x = 0; x < this.length; x++) {
	if (this[x] == obj) {
		this.splice(x, 1);
		return true;
	}
}

return false;
};

Array.prototype.indexOf = function(obj)
{
for (var x = 0; x < this.length; x++) {	
	if (this[x] == obj)
		return x;
}

return -1;
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
form.oldaction = form.action;
form.action = url;
 
// Execute the form validation - if any
if (form.onsubmit) {
	var submitOK = form.onsubmit();
	if (!submitOK) {
		form.action = form.oldaction;
		delete form.oldaction;
		return false;
	}
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
var intValue = parseFloat(text.value);
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
if (combo.selectedIndex === 0) {
	alert('Please provide the ' + title + '.');
	combo.focus();
	return false;
}

return true;
}

function validateFile(fileName, extType, title)
{
if ((!fileName) || (fileName.disabled) || (fileName.value.length === 0)) return true;
var extTypes = extType.toLowerCase().split(',');

var fName = fileName.value;
var ext = fName.substring(fName.lastIndexOf('.') + 1).toLowerCase();
for (var x = 0; x < extTypes.length; x++) {
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
for (var x = 0; x < checkbox.length; x++) {
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
var daysInMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
var month = parseInt(combo.options[combo.selectedIndex].value, 10);

var dCombo = document.forms[0].dateD;
dCombo.options.length = daysInMonth[month];
for (var x = 1; x <= daysInMonth[month]; x++) {
	dCombo.options[x-1] = new Option(x);
}
	
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
	var MSXML_XMLHTTP_PROGIDS = ['MSXML2.XMLHTTP.5.0','MSXML2.XMLHTTP.4.0','MSXML2.XMLHTTP.3.0','MSXML2.XMLHTTP','Microsoft.XMLHTTP'];
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
var anchors = document.getElementsByTagName('a');
for (var i = 0; i < anchors.length; i++) {
	var anchor = anchors[i];
	if (anchor.getAttribute('href')) {
		var rel = anchor.getAttribute('rel');
		if ((rel == 'external') || (rel == 'nofollow'))
			anchor.target = '_blank';
	}
}

return true;
}

function toggleExpand(lnk, className)
{
var isDisplayed = (lnk.innerHTML == 'COLLAPSE');
lnk.innerHTML = isDisplayed ? 'EXPAND' : 'COLLAPSE';
var rows = getElementsByClass(className);
for (var y = 0; y < rows.length; y++) {
	var row = rows[y];
	row.style.display = isDisplayed ? 'none' : '';
}

return true;
}

function showHelp(helpname)
{
var URLflags = 'height=460,width=660,menubar=no,toolbar=no,status=no,scrollbars=yes,resizable=yes';
var w = window.open('/help.do?id=' + helpname, 'HELP', URLflags);
return true;
}
