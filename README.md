# react-native-react-native-u2f

## Getting started

`$ npm install react-native-react-native-u2f --save`

### Mostly automatic installation

`$ react-native link react-native-react-native-u2f`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-react-native-u2f` and add `ReactNativeU2f.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libReactNativeU2f.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.ReactNativeU2fPackage;` to the imports at the top of the file
  - Add `new ReactNativeU2fPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-u2f'
  	project(':react-native-react-native-u2f').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-u2f/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-u2f')
  	```


## Usage
```javascript
import ReactNativeU2f from 'react-native-react-native-u2f';

// TODO: What to do with the module?
ReactNativeU2f;
```
