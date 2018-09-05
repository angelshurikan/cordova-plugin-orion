cordova.define("cordova-plugin-orion.Orion", function(require, exports, module) {
var exec = require('cordova/exec');

function Orion() {
};

Orion.alert = function (content) {
    window.alert(content);
};

Orion.blockStatusBarOverlay = function (success, error) {
    exec(success, error, "Orion", "blockStatusBarOverlay");
};

Orion.checkConnectedDevices = function (success, error) {
    exec(success, error, "Orion", "checkConnectedDevices");
};

Orion.checkHotspot = function (success, error) {
    exec(success, error, "Orion", "checkHotspot");
};

Orion.coolMethod = function (arg0, success, error) {
    exec(success, error, "Orion", "coolMethod", [arg0]);
};

Orion.getApps = function (success, error) {
    exec(success, error, "Orion", "getApps");
};

Orion.getBrightness = function (number, success, error) {
    exec(success, error, "Orion", "getBrightness", [number]);
};

Orion.getCall = function (number, success, error) {
    exec(success, error, "Orion", "getCall", [number]);
};

Orion.getInfo = function (success, error) {
    exec(success, error, "Orion", "getInfo");
};

Orion.isDataActive = function (success, error) {
    exec(success, error, "Orion", "isDataActive");
};

Orion.permAccessibilityService = function (success, error) {
    exec(success, error, "Orion", "permAccessibilityService");
};

Orion.setBrightness = function (auto, brightness, success, error) {
    exec(success, error, "Orion", "setBrightness", [auto, brightness]);
};

Orion.setHotspot = function (ssid, pwd, statut, success, error) {
    exec(success, error, "Orion", "setHotspot", [ssid, pwd, statut]);
};

module.exports = Orion;
});
