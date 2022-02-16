
package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.os.Bundle;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.provider.Settings;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;

import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;

public class RNFloatingBubbleModule extends ReactContextBaseJavaModule {

  private BubblesManager bubblesManager;
  private final ReactApplicationContext reactContext;
  private BubbleLayout bubbleView;
  private ImageView btnClose;
  private Button btn1;
  private TextView text1;
  private TextView text2;
  public RNFloatingBubbleModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;

    // try {
    //   initializeBubblesManager();
    // } catch (Exception e) {

    // }
  }

  @Override
  public String getName() {
    return "RNFloatingBubble";
  }
  @ReactMethod
  public void reopenApp(){
    Intent launchIntent = reactContext.getPackageManager().getLaunchIntentForPackage(reactContext.getPackageName());
    if (launchIntent != null) {
      reactContext.startActivity(launchIntent);
    }
  }
  @ReactMethod // Notates a method that should be exposed to React
  public void showFloatingBubble(double x, double y, final Promise promise) {
    try {
      this.addNewBubble(x, y);
      promise.resolve("");
    } catch (Exception e) {
      promise.reject(e);
    }
  }  
  @ReactMethod // Notates a method that should be exposed to React
  public void showFeedBackFloating(double x, double y, final Promise promise) {
    try {
      this.addNewBubble(x, y);
      promise.resolve("");
    } catch (Exception e) {
      promise.reject(e);
    }
  }  

  @ReactMethod // Notates a method that should be exposed to React
  public void hideFloatingBubble(final Promise promise) {
    try {
      this.removeBubble();
      promise.resolve("");
    } catch (Exception e) {
      promise.reject("");
    }
  }  
  
  @ReactMethod // Notates a method that should be exposed to React
  public void requestPermission(final Promise promise) {
    try {
      this.requestPermissionAction(promise);
    } catch (Exception e) {
    }
  }  
  
  @ReactMethod // Notates a method that should be exposed to React
  public void checkPermission(final Promise promise) {
    try {
      promise.resolve(hasPermission());
    } catch (Exception e) {
      promise.reject("");
    }
  }  
  
  @ReactMethod // Notates a method that should be exposed to React
  public void initialize(final Promise promise) {
    try {
      this.initializeBubblesManager();
      promise.resolve("");
    } catch (Exception e) {
      promise.reject("");
    }
  }  

  private void addNewBubble(double x, double y) {
    this.removeBubble();
    bubbleView = (BubbleLayout) LayoutInflater.from(reactContext).inflate(R.layout.bubble_layout, null);
    bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
      @Override
      public void onBubbleRemoved(BubbleLayout bubble) {
        bubbleView = null;
        sendEvent("floating-bubble-remove");
      }
    });
    btn1 = (Button) bubbleView.findViewById(R.id.btn1);
    text1 = (TextView) bubbleView.findViewById(R.id.textViewOrderDistance);
    text1.append("Distância da coleta: " + x + "km");
    text2 = (TextView) bubbleView.findViewById(R.id.textViewOrderTotalDistance);
    text2.append("Percurso total: " + y + "km");
    btnClose = (ImageView) bubbleView.findViewById(R.id.imageViewClose);
    btnClose.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        removeBubble();
      }
    });
    btn1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendEvent("aceitar pedido");
      }
    });
    bubbleView.setShouldStickToWall(true);
    bubblesManager.addBubble(bubbleView, 20, 20);
  }

  private boolean hasPermission(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return Settings.canDrawOverlays(reactContext);
    }
    return true;
  }

  private void removeBubble() {
    if(bubbleView != null){
      try{
        bubblesManager.removeBubble(bubbleView);
      } catch(Exception e){

      }
    }
  }


  public void requestPermissionAction(final Promise promise) {
    if (!hasPermission()) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + reactContext.getPackageName()));
      Bundle bundle = new Bundle();
      reactContext.startActivityForResult(intent, 0, bundle);
    } 
    if (hasPermission()) {
      promise.resolve("");
    } else {
      promise.reject("");
    }
  }

  private void initializeBubblesManager() {
    bubblesManager = new BubblesManager.Builder(reactContext).setTrashLayout(R.layout.bubble_trash_layout)
        .setInitializationCallback(new OnInitializedCallback() {
          @Override
          public void onInitialized() {
            // addNewBubble();
          }
        }).build();
    bubblesManager.initialize();
  }

  private void sendEvent(String eventName) {
    WritableMap params = Arguments.createMap();
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }
}