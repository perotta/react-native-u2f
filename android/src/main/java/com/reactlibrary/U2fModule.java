package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;

import android.content.IntentSender;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.u2f.U2fApiClient;
import com.google.android.gms.fido.u2f.U2fPendingIntent;
import com.google.android.gms.fido.u2f.api.common.RegisterRequestParams;
import com.google.android.gms.fido.u2f.api.common.RegisterRequest;
import com.google.android.gms.fido.u2f.api.common.ResponseData;
import com.google.android.gms.fido.u2f.api.common.SignRequestParams;
import com.google.android.gms.fido.u2f.api.common.RegisteredKey;
import com.google.android.gms.fido.u2f.api.common.KeyHandle;

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
    private static final String E_REGISTER_CANCELLED = "E_REGISTER_CANCELLED";
    private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";

    private Promise mSignPromise;
    private Promise mRegisterPromise;

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
            } else if (requestCode == REQUEST_CODE_REGISTER) {
                if (mRegisterPromise != null) {
                  if (resultCode == Activity.RESULT_CANCELED) {
                      mRegisterPromise.reject(E_REGISTER_CANCELLED, "Register was cancelled");
                  } else if (resultCode == Activity.RESULT_OK) {
                      Log.i(TAG, "Received response from Security Key");
                      ResponseData response = intent.getParcelableExtra(Fido.KEY_RESPONSE_EXTRA);
                      mRegisterPromise.resolve(response.toJsonObject().toString());
                  }
                }
                mRegisterPromise = null;
            }
        }
    };

    public U2fModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "U2f";
    }

    @ReactMethod
    public void nativeRegister(ReadableArray registerRequestsIN, ReadableArray registeredKeysIN, Double timeoutSeconds, final Promise promise) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
          promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
          return;
        }

        mRegisterPromise = promise;

        try{
          RegisterRequestParams.Builder builder = new RegisterRequestParams.Builder();
          ArrayList<RegisterRequest> registerRequestsOUT = new ArrayList<RegisterRequest>();

          for( int i = 0; i < registerRequestsIN.size(); i++) {
              ReadableMap request = registerRequestsIN.getMap(i);
              JSONObject registerRequestsJson = new JSONObject();
              registerRequestsJson.put("challenge", request.getString("challenge"));
              registerRequestsJson.put("version", request.getString("version"));
              registerRequestsJson.put("appId", request.getString("appId"));
              RegisterRequest registerRequest = RegisterRequest.parseFromJson(registerRequestsJson);
              registerRequestsOUT.add(registerRequest);
          }
          builder.setRegisterRequests(registerRequestsOUT);


          ArrayList<RegisteredKey> registeredKeysOUT = new ArrayList<RegisteredKey>();

          for( int i = 0; i < registeredKeysIN.size(); i++) {
              ReadableMap key = registeredKeysIN.getMap(i);
              JSONObject registeredKeysJson = new JSONObject();
              registeredKeysJson.put("challenge", key.getString("challenge"));
              registeredKeysJson.put("version", key.getString("version"));
              registeredKeysJson.put("appId", key.getString("appId"));
              registeredKeysJson.put("keyHandle", key.getString("keyHandle"));
              RegisteredKey registeredKey = RegisteredKey.parseFromJson(registeredKeysJson);
              registeredKeysOUT.add(registeredKey);
          }

          builder.setRegisteredKeys(registeredKeysOUT);


          builder.setTimeoutSeconds(timeoutSeconds);
          RegisterRequestParams registerRequestParams = builder.build();
          U2fApiClient mU2fApiClient = new U2fApiClient(getReactApplicationContext());
          Task<U2fPendingIntent> result = mU2fApiClient.getRegisterIntent(registerRequestParams);


          result.addOnSuccessListener(
              new OnSuccessListener<U2fPendingIntent>() {
                  @Override
                  public void onSuccess(U2fPendingIntent mU2fPendingIntent) {
                      if (mU2fPendingIntent.hasPendingIntent()) {
                          try {
                              mU2fPendingIntent.launchPendingIntent(getCurrentActivity(), REQUEST_CODE_REGISTER);
                          } catch (IntentSender.SendIntentException e) {
                              Log.i(TAG, "Error launching pending intent for register request");
                              promise.reject("Error launching pending intent for register request", e);
                          }
                      }
                  }
          });

        } catch( Exception e ){
          promise.reject("REGISTER_FAILED", e);
        }
    }


    @ReactMethod
    public void nativeSign(ReadableArray registeredKeysIN, Double timeoutSeconds, final Promise promise) {

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
          promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
          return;
        }

        mSignPromise = promise;

        try{

          SignRequestParams.Builder builder = new SignRequestParams.Builder();

          ArrayList<RegisteredKey> registeredKeysOUT = new ArrayList<RegisteredKey>();

          for( int i = 0; i < registeredKeysIN.size(); i++) {
              ReadableMap key = registeredKeysIN.getMap(i);
              JSONObject registeredKeysJson = new JSONObject();
              registeredKeysJson.put("challenge", key.getString("challenge"));
              registeredKeysJson.put("version", key.getString("version"));
              registeredKeysJson.put("appId", key.getString("appId"));
              registeredKeysJson.put("keyHandle", key.getString("keyHandle"));

              RegisteredKey registeredKey = RegisteredKey.parseFromJson(registeredKeysJson);

              registeredKeysOUT.add(registeredKey);
          }

          builder.setRegisteredKeys(registeredKeysOUT);
          builder.setTimeoutSeconds(timeoutSeconds);
          SignRequestParams signRequestParams = builder.build();

          // DEBUGGING
          /*
          List<RegisteredKey> registeredKeysDEBUG = signRequestParams.getRegisteredKeys();
          for( int i = 0; i < registeredKeysDEBUG.size(); i++) {
            Log.i(TAG, "KEY INFO");
            Log.i(TAG, "APP ID " + registeredKeysDEBUG.get(i).getAppId() );
            Log.i(TAG, "CHALLENGE " + registeredKeysDEBUG.get(i).getChallengeValue() );
            KeyHandle keyHandledebug = registeredKeysDEBUG.get(i).getKeyHandle();
            Log.i(TAG, "KEY HANDLE " + keyHandledebug.toString() );
          }
          */
          // DEBUGGING


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
