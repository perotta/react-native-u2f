package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;

import android.content.IntentSender;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import com.google.android.gms.fido.common.Transport;
import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.u2f.U2fApiClient;
import com.google.android.gms.fido.u2f.U2fPendingIntent;
import com.google.android.gms.fido.u2f.api.common.ErrorResponseData;
import com.google.android.gms.fido.u2f.api.common.RegisterRequestParams;
import com.google.android.gms.fido.u2f.api.common.RegisterResponseData;
import com.google.android.gms.fido.u2f.api.common.ResponseData;
import com.google.android.gms.fido.u2f.api.common.SignRequestParams;
import com.google.android.gms.fido.u2f.api.common.SignResponseData;
import com.google.android.gms.fido.u2f.api.common.RegisteredKey;
import com.google.android.gms.fido.u2f.api.common.KeyHandle;
import com.google.android.gms.fido.u2f.api.common.ProtocolVersion;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

public class U2fModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static final String TAG = "ReactNativeU2fModule";
    private static final int REQUEST_CODE_REGISTER = 0;
    private static final int REQUEST_CODE_SIGN = 1;
    private static final String E_SIGN_CANCELLED = "E_SIGN_CANCELLED";
    private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";

    private Promise mSignPromise;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == REQUEST_CODE_SIGN) {
                if (mSignPromise != null) {
                  if (resultCode == Activity.RESULT_CANCELED) {
                      mSignPromise.reject(E_SIGN_CANCELLED, "Sign was cancelled");
                  } else if (resultCode == Activity.RESULT_OK) {
                      Log.i(TAG, "Received response from Security Key");
                      ResponseData response = intent.getParcelableExtra(Fido.KEY_RESPONSE_EXTRA);
                      mSignPromise.resolve(response.toJsonObject().toString());
                  }
                }
                mSignPromise = null;
            }
        }
    };

    public U2fModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        // Add the listener for `onActivityResult`
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "U2f";
    }


    @ReactMethod
    public void nativeSign(ReadableArray registeredKeysIN, final Promise promise) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
          promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
          return;
        }

        // Store the promise to resolve/reject when picker returns data
        mSignPromise = promise;

        try{

          //This could be specific to each key, but for simplicity purpose it will be standard for every key
          ArrayList<Transport> transports = new ArrayList<Transport>();

          SignRequestParams.Builder builder = new SignRequestParams.Builder();

          ArrayList<RegisteredKey> registeredKeysOUT = new ArrayList<RegisteredKey>();

          for( int i = 0; i < registeredKeysIN.size(); i++) {
              ReadableMap key = registeredKeysIN.getMap(i);
              ProtocolVersion version;
              if( key.getString("version").equals("U2F_V1") ){
                  version = ProtocolVersion.V1;
              } else if( key.getString("version").equals("U2F_V2") ){
                  version = ProtocolVersion.V2;
              } else {
                  version = ProtocolVersion.UNKNOWN;
              }

              KeyHandle keyHandle = new KeyHandle(key.getString("keyHandle").getBytes("UTF-8"),version, transports);
              RegisteredKey registeredKey = new RegisteredKey(keyHandle, key.getString("challenge"), key.getString("appId"));
              registeredKeysOUT.add(registeredKey);
          }

          builder.setRegisteredKeys(registeredKeysOUT);
          SignRequestParams signRequestParams = builder.build();

          Log.i(TAG, "SignRequestParams built");

          U2fApiClient mU2fApiClient = new U2fApiClient(getReactApplicationContext());
          Task<U2fPendingIntent> result = mU2fApiClient.getSignIntent(signRequestParams);

          result.addOnSuccessListener(
              new OnSuccessListener<U2fPendingIntent>() {
                  @Override
                  public void onSuccess(U2fPendingIntent mU2fPendingIntent) {
                      if (mU2fPendingIntent.hasPendingIntent()) {
                          try {
                              mU2fPendingIntent.launchPendingIntent(getCurrentActivity(), REQUEST_CODE_SIGN);
                          } catch (IntentSender.SendIntentException e) {
                              Log.i(TAG, "Error launching pending intent for sign request");
                              promise.reject("Error launching pending intent for sign request", e);
                          }
                      }
                  }
          });

        } catch( Exception e ){
          promise.reject("SING_FAILED", e);
        }
    }
}
