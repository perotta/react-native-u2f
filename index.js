/*

  By using this code you accept the condition below:
  You assume all risks of the any harm that this code can make to your application.
  The author does not take any responsability for bug, error or misfunction on this code.

  Use at your own risk.

*/
import { NativeModules } from "react-native";

const { U2f } = NativeModules;

const errorNames = {
  "1": "OTHER_ERROR",
  "2": "BAD_REQUEST",
  "3": "CONFIGURATION_UNSUPPORTED",
  "4": "DEVICE_INELIGIBLE",
  "5": "TIMEOUT"
};

const ReactNativeU2fApi = {
  register: function(registerRequests, registeredKeys, timeout = 60) {
    return new Promise((resolve, reject) => {
      U2f.nativeRegister(registerRequests, registeredKeys, timeout)
        .then(resultString => {
          const result = JSON.parse(resultString);
          if ("errorCode" in result) {
            const msg =
              "errorMessage" in result
                ? result.errorMessage
                : "Security Key responded with errorCode";
            const error = new Error(msg);
            error.metaData = {
              type: errorNames[result.errorCode],
              code: result.errorCode
            };
            reject(error);
          } else {
            resolve(resultString);
          }
        })
        .catch(e => {
          const error = new Error(e.message);
          error.metaData = {
            type: "NATIVE_ERROR",
            code: 0
          };
          reject(error);
        });
    });
  },
  sign: function(registeredKeys, timeout = 60) {
    return new Promise((resolve, reject) => {
      U2f.nativeSign(registeredKeys, timeout)
        .then(resultString => {
          const result = JSON.parse(resultString);
          if ("errorCode" in result) {
            const msg =
              "errorMessage" in result
                ? result.errorMessage
                : "Security Key responded with errorCode";
            const error = new Error(msg);
            error.metaData = {
              type: errorNames[result.errorCode],
              code: result.errorCode
            };
            reject(error);
          } else {
            resolve(resultString);
          }
        })
        .catch(e => {
          const error = new Error(e.message);
          error.metaData = {
            type: "NATIVE_ERROR",
            code: 0
          };
          reject(error);
        });
    });
  }
};

export default ReactNativeU2fApi;
