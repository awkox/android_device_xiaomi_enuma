/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.pad_parts.keyboard;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.lineageos.pad_parts.R;

public class KeyboardService extends Service implements KeyboardObserver.KeyboardListener {

    private static final String TAG = "KeyboardService";
    private static final boolean DEBUG = true;

    private KeyboardObserver mObserver;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        super.onCreate();

        mObserver = new KeyboardObserver(this);

        mObserver.startWatching();
        KeyboardUtils.enableKeyboard(true);

        // The observer only reports from FileObserver MODIFY events, so a
        // service that starts with the case already attached would never
        // apply the IME policy until the next event. Apply it now.
        onKeyboardConnected(KeyboardUtils.isKeyboardConnected());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();

        mObserver.stopWatching();
        KeyboardUtils.enableKeyboard(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onKeyboardConnected(boolean connected) {
        KeyboardUtils.setShowImeWithHardKeyboard(this, !connected);
    }
}
