/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.pad_parts.keyboard;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

import java.util.Objects;

public class KeyboardObserver extends FileObserver {

    private static final String TAG = "KeyboardObserver";
    private static final boolean DEBUG = true;

    interface KeyboardListener {
        void onKeyboardConnected(boolean connected);
    }

    private final KeyboardListener listener;

    private Boolean mCurrentStatus = null;

    public KeyboardObserver(KeyboardListener listener) {
        super(KeyboardUtils.KEYBOARD_STATUS_PATH, FileObserver.MODIFY);

        this.listener = listener;
    }

    @Override
    public void onEvent(int event, String path) {
        boolean status = KeyboardUtils.isKeyboardConnected();

        if (!Objects.equals(mCurrentStatus, status)) {
            mCurrentStatus = status;

            if (DEBUG) Log.d(TAG, String.format("connection status changed: %b", mCurrentStatus));
        } else {
            return;
        }

        listener.onKeyboardConnected(status);
    }
}
