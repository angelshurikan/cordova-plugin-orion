var exec = require('cordova/exec');

function Orion() {
};

Orion.alert = function (content) {
    window.alert(content);
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

Orion.getInfo = function (success, error) {
    exec(success, error, "Orion", "getInfo");
};

Orion.getApps = function (success, error) {
    exec(success, error, "Orion", "getApps");
};

Orion.getCall = function (number, success, error) {
    exec(success, error, "Orion", "getCall", [number]);
};

Orion.getBrightness = function (number, success, error) {
    exec(success, error, "Orion", "getBrightness", [number]);
};

Orion.isDataActive = function (success, error) {
    exec(success, error, "Orion", "isDataActive");
};

Orion.runServices = function (success, error) {
    exec(success, error, "Orion", "launchService");
};

Orion.setHotspot = function (ssid, pwd, statut, success, error) {
    exec(success, error, "Orion", "setHotspot", [ssid, pwd, statut]);
};



module.exports = Orion;