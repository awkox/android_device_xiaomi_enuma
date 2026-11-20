/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.dspvolume.xiaomi;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class VolumeListenerService extends Service {

    private VolumeListenerReceiver mReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Register once for the lifetime of the service. Doing it in
        // onStartCommand leaked a receiver on every restart, and each
        // volume change then reached the DSP once per instance.
        mReceiver = new VolumeListenerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AudioManager audioManager = getSystemService(AudioManager.class);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setParameters("volume_change=" + current + ";flags=6");

        // The DSP sync must survive the process being reclaimed, so state
        // the policy instead of inheriting it from Service.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        super.onDestroy();
    }
}
