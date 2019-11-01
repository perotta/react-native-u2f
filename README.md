# react-native-u2f

This module is a simple wrapper to Android's U2fApiClient
https://developers.google.com/android/reference/com/google/android/gms/fido/u2f/U2fApiClient

There is no implementation for iOS. Feel free to contribute with an iOS native module.

## Getting started

### React Native 0.60 or later

Simply install with:

`$ yarn add react-native-u2f`
or
`$ npm install react-native-u2f`

### React Native before 0.60

`$ npm install react-native-u2f --save`

#### Mostly automatic installation

`$ react-native link react-native-u2f`

#### Manual installation

1. Open up `android/app/src/main/java/[...]/MainApplication.java`

- Add `import com.reactlibrary.U2fPackage;` to the imports at the top of the file
- Add `new U2fPackage()` to the list returned by the `getPackages()` method

2. Append the following lines to `android/settings.gradle`:
   ```
   include ':react-native-u2f'
   project(':react-native-u2f').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-u2f/android')
   ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
   ```
     compile project(':react-native-u2f')
   ```

## Usage

### Error codes

```javascript
//Some error occurred on the Java code
const errorNames = {
  "0": "NATIVE_ERROR"
};

//Those are from FIDO specs and will come from the security key
const errorNames = {
  "1": "OTHER_ERROR",
  "2": "BAD_REQUEST",
  "3": "CONFIGURATION_UNSUPPORTED",
  "4": "DEVICE_INELIGIBLE",
  "5": "TIMEOUT"
};
```

### Sign

```javascript
import React from "react";
import u2f from "react-native-u2f";

function SampleComponent(props) {
  React.useEffect(() => {
    // ...
    // Call your server to get registered keys info

    //registeredKeys is a collection (array of objects). Each object need to have all these 4 keys (challenge, appId, version and keyHandle)
    const registeredKeys_MANDATORY = [
      {
        challenge:
          "2e4c8364b5e51d79b18a6c0da9da79dae6c2b54681d1bf3d204aec0e9bd8db7a",
        appId: "https://your.server/app-id.json",
        version: "U2F_V2",
        keyHandle:
          "oZYCAaj9ILtqDB54SBK2MpN9dSBKPPUa0fP0lLuvVVovVRgWXXmLZGCUjfV5KSWWneGWTUTYW36O2Dlq2zDv-Q"
      }
      //... As many registered keys you have
    ];

    const timeout_OPTIONAL = 59; //request timeout in seconds. Default is 60 seconds

    u2f
      .sign(registeredKeys_MANDATORY, timeout_OPTIONAL)
      .then(res => {
        // If it reaches here, the security key responded with no error.
        // "res" is a JSON string containing the security key response
        // ...
        // Now post your "res" string to your authentication server
      })
      .catch(err => {
        // Errors can come from security key (error codes 1, 2, 3, 4 or 5) or from the native code (error code 0)
        // err is an Error object
        // err.message -> Can be specific of just a standard message
        // err.metaData.code -> error code (see above)
        // err.metaData.type -> error description (see above)
      });
  }, []);
}
```

### Register

```javascript
import React from "react";
import u2f from "react-native-u2f";

function SampleComponent(props) {
  React.useEffect(() => {
    // ...
    // Call your server to get register requests and registered keys info

    //registeredKeys is a collection (array of objects). Each object need to have all these 3 keys (challenge, appId, version)
    const registerRequests_MANDATORY = [
      {
        challenge:
          "2e4c8364b5e51d79b18a6c0da9da79dae6c2b54681d1bf3d204aec0e9bd8db7a",
        appId: "https://your.server/app-id.json",
        version: "U2F_V2"
      }
    ];

    //registeredKeys is same thing as on sign
    const registeredKeys_MANDATORY = [
      // ...
    ];

    const timeout_OPTIONAL = 59; //request timeout in seconds. Optional. Default is 60 seconds

    u2f
      .register(
        registerRequests_MANDATORY,
        registeredKeys_MANDATORY,
        timeout_OPTIONAL
      )
      .then(res => {
        // If it reaches here, the security key responded with no error.
        // "res" is a JSON string containing the security key response
        // ...
        // Now post your "res" string to your authentication server
      })
      .catch(err => {
        // Errors can come from security key (error codes 1, 2, 3, 4 or 5) or from the native code (error code 0)
        // err is an Error object
        // err.message -> Can be specific of just a standard message
        // err.metaData.code -> error code (see above)
        // err.metaData.type -> error description (see above)
      });
  }, []);
}
```

## Credits

Credits for inspiration/parts of the code in this package:

u2f-api
https://github.com/grantila/u2f-api

Android security-samples
https://github.com/android/security-samples/tree/master/Fido

React Native - Native Modules Guide
https://facebook.github.io/react-native/docs/native-modules-android
