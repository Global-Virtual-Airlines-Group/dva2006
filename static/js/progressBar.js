/**
 * @name ProgressbarControl
 * @version 1.0
 * @author Bjorn BRala
 * @copyright (c) 2008 SWIS BV - www.geostart.nl
 * @fileoverview Creates a progress bar control for usage in google maps. 
 *   It can be used to show the progress of loading markers, for example.
 */

/**
 * @name ProgressbarOptions
 * @class This class represents optional arguments to 
 *   {@link ProgressbarControl}, 
 * @property {Number} [width=176] Specifies, in pixels, the width of the bar.
 * @property {String} [loadstring=Loading...] Specifies the string displayed 
 *  when first starting the control, before any update.
 */


/**
 * Custom progress bar control, for placement on map.
 * 
 * @private
 * @return {GControl}
 */    
function ProgressbarMapControl(map, width, color) { 
  this.map_ = map; 
  this.width_ = width;
  this.color_ = color;
}

ProgressbarMapControl.prototype = new GControl(true, false);

/**
 * @private
 * @desc Initializes the GControl. Creates the HTML and styles.
 * @return {Element}
 */
ProgressbarMapControl.prototype.initialize = function () {
  var container_ = document.createElement('div');
  container_.innerHTML = '<div style="position:absolute;width:100%;border:5px;'
    + 'text-align:center;vertical-align:bottom;" id="geo_progress_text"></div>'
    + '<div style="background-color:' + this.color_ + ';height:100%;" id="geo_progress"></div>';
  container_.id = "geo_progress_container";
  container_.style.display = "none";
  container_.style.width = this.width_ + "px";
  container_.style.fontSize = "0.8em";
  container_.style.height = "1.3em";
  container_.style.border = "1px solid #555";
  container_.style.backgroundColor = "white";
  container_.style.textAlign = "left";
  this.map_.getContainer().appendChild(container_);
  return container_;
};

/**
 * @private 
 * @desc Return the default position for the control
 * @return {GControlPosition}
 */
ProgressbarMapControl.prototype.getDefaultPosition = function () {
  return new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(30, 56));
};

/**
 * Creates a progress bar control on the given map, with the given options.
 *
 * @constructor
 * @param {GMap2} Map object
 * @param  {ProgressbarOptions} opt_opts
 */
function ProgressbarControl(map, opt_opts) {
  this.options_ = opt_opts || {};

  this.width_ = this.options_.width || 176;
  this.loadstring_ = this.options_.loadstring || 'LOADING';
  this.color_ = this.options_.color || 'blue';

  this.control_ = new ProgressbarMapControl(map, this.width_, this.color_);
  this.map_ = map;
  this.map_.addControl(this.control_);
  this.div_ = document.getElementById('geo_progress');
  this.text_ = document.getElementById('geo_progress_text');
  this.container_ = document.getElementById('geo_progress_container');
  this.operations_ = 0;
  this.current_ = 0;
}

/**
 * @desc Start the progress bar. 
 * @param {Number} operations Total amount of operations that will be executed.
 */
ProgressbarControl.prototype.start = function (operations) {
  this.div_.style.width = '0%'; 
  this.operations_ = operations || 0;
  this.current_ = 0;
  this.text_.style.color = "#111";
  this.text_.innerHTML = this.loadstring_;
  this.container_.style.display = "block";
};

/**
 * @desc  Update the progress with specified number of operations.
 * @param {Number} step Number of operations to add to bar.
 */
ProgressbarControl.prototype.updateLoader = function (step) {
  this.current_ += step;
  if (this.current_ > 0) {
    var percentage_ = Math.ceil((this.current_ / this.operations_) * 100);
    if (percentage_ > 100) { 
      percentage_ = 100; 
    }
    this.div_.style.width = percentage_ + '%'; 
    this.text_.innerHTML = this.current_ + ' / ' + this.operations_;
  }
};

/**
 * @desc Remove control.
 */
ProgressbarControl.prototype.remove = function () {
  this.container_.style.display = 'none';
};
