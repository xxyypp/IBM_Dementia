package com.example.shreyus.myapp;

import android.content.Context;

public interface BLEManager {
    void log(String message);
    void onConnectionStateChange(int newState);
    Context getContext();
    void enableBluetooth();
    void onConnected();
    void onDisconnected();
    boolean checkPermission();
}
