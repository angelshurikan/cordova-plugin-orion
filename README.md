# Cordova plugin Orion

## Install

    cordova plugin add cordova-plugin-orion

## Method
**alert**

    cordova.plugins.Orion.alert(content)
    var success = function(data){}
    var error = function(e){}

**checkConnectedDevices**

    cordova.plugins.Orion.checkConnectedDevices(success, error)
    var success = function(data){}
    var error = function(e){}
    
**checkHotspot**

    cordova.plugins.Orion.checkHotspot(success, error)
    var success = function(data){}
    var error = function(e){}
    
**coolMethod**

    cordova.plugins.Orion.coolMethod(arg0, success, error)
    var success = function(data){}
    var error = function(e){}
    
**getApps**
List of applications installed in the phone

    cordova.plugins.Orion.getApps(success, error)
    var success = function(data){}
    var error = function(e){}
    
**getCall**

Call a phone number with the default phone application  

    cordova.plugins.Orion.getCall(number, success, error)
    var success = function(data){}
    var error = function(e){}
    
**getBrightness**

    cordova.plugins.Orion.getBrightness(success, error)
    var success = function(data){}
    var error = function(e){}
    
**getInfo**

    cordova.plugins.Orion.getInfo(success, error)
    var success = function(data){}
    var error = function(e){}
    
**isDataActive**

    cordova.plugins.Orion.isDataActive(success, error)
    var success = function(data){}
    var error = function(e){} 
    
**runServices**

    cordova.plugins.Orion.runServices(success, error)
    var success = function(data){}
    var error = function(e){}
    
**setHotspot**

    cordova.plugins.Orion.setHotspot(ssid, pwd, statut, success, error)
    var success = function(data){}
    var error = function(e){}

##Publish npm

    npm publish