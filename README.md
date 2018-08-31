# Cordova plugin Orion

## Install

    cordova plugin add cordova-plugin-orion

## Method
**alert**

    cordova.plugins.Orion.alert(content)

**checkConnectedDevices**

    cordova.plugins.Orion.checkConnectedDevices(success, error)
    
**checkHotspot**

    cordova.plugins.Orion.checkHotspot(success, error)
    
**coolMethod**

    cordova.plugins.Orion.coolMethod(arg0, success, error)
    
**getInfo**

    cordova.plugins.Orion.getInfo(success, error)
    
**getApps**
List of applications installed in the phone

    cordova.plugins.Orion.getApps(success, error)
    var success = function(listApps){}
    var error = function(e){}
    
**isDataActive**

    cordova.plugins.Orion.isDataActive(success, error) 
    
**getCall**

Call a phone number with the default phone application  

    cordova.plugins.Orion.getCall(number, success, error)
    var success = function(){}
    var error = function(e){}
    
**runServices**

    cordova.plugins.Orion.runServices(success, error)
    
**setHotspot**

    cordova.plugins.Orion.setHotspot(ssid, pwd, statut, success, error)

##Publish npm

    npm publish