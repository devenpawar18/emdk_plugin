var cordova = require('cordova');
var exec = require('cordova/exec');

 /**
         * Constructor.
 *
 * @returns {DataWedge}
 */
function DataWedge() {

};

/**
 * Turn on DataWedge (default profile) and listen for event.  Listens for hardward button events.
 * 
 * @param successCallback - Success function should expect a barcode to be passed in
 * @param intentAction - action to listen for.  This is what you configured in the DataWedge app.  
 *      
 */
DataWedge.prototype.start = function (intentAction) {
    var args = [];
    if (intentAction) {
        args[0] = intentAction;
    }
    exec(null, null, 'SymbolDataWedge', 'start', args);
};
/**
 * Turn off DataWedge plugin
 */
DataWedge.prototype.stop = function () {
  
    exec(null, null, 'SymbolDataWedge', 'stop', []);
};

/**
 * Activate a different profile for the data wedge.  For instance, to enable data processing rules
 */
DataWedge.prototype.switchProfile = function (profileName) {
    if (!profileName)  {
        console.log("DataWedge.switchProfile did not include a profile.  A profile name is required.");
        return;
    }
    exec(null, null, 'SymbolDataWedge', 'switchProfile', [profileName]);
};


/**
 * Register a callback for scan events.  This function will be called when barcdoes are read
 */
DataWedge.prototype.registerForBarcode = function (callback) {
    
    exec(callback, null, 'SymbolDataWedge', 'scanner.register', []);
};

/**
 * De-register a callback for scan events.  
 */
DataWedge.prototype.unregisterBarcode = function () {
    
    exec(null, null, 'SymbolDataWedge', 'scanner.unregister', []);
};

/**
 * Manually turn on barcode scanner
 */
DataWedge.prototype.startScanner = function () {
    
    exec(null, null, 'SymbolDataWedge', 'scanner.softScanOn', []);
};

/**
 * Manually turn off barcode scanner
 */
DataWedge.prototype.stopScanner = function () {
    exec(null, null, 'SymbolDataWedge', 'scanner.softScanOff', []);
};




//create instance
var DataWedge = new DataWedge();

module.exports = DataWedge;