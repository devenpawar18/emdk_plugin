var cordova = require('cordova');
var exec = require('cordova/exec');

 /**
         * Constructor.
 *
 * @returns {SymbolEMDK}
 */
function SymbolEMDK() {

};


/**
 * Manually turn on barcode scanner
 */
SymbolEMDK.prototype.startScanner = function () {
    
    exec(null, null, 'SymbolEMDK', 'scanner.softScanOn', []);
};



//create instance
var SymbolEMDK = new SymbolEMDK();

module.exports = SymbolEMDK;