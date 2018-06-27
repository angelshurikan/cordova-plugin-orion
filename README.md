#Cordova plugin Orion

##Install

    cordova plugin add cordova-plugin-orion

##Method
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

    cordova.plugins.Orion.getApps(success, error)  
    
**isDataActive**

    cordova.plugins.Orion.isDataActive(success, error) 
    
**getCall**

Call a phone number with the default phone application  

    cordova.plugins.Orion.getCall(number, success, error)
    
**runServices**

    cordova.plugins.Orion.runServices(success, error)
    
**setHotspot**

    cordova.plugins.Orion.setHotspot(ssid, pwd, statut, success, error)

##Publish npm

    npm publish