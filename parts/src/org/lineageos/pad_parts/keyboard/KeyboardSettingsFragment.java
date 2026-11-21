/*
 * Copyright (C) The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.pad_parts.keyboard;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import org.lineageos.pad_parts.R;
import org.lineageos.pad_parts.utils.SettingsUtils;

public class KeyboardSettingsFragment extends PreferenceFragment {
    private static final String TAG = "KeyboardFragment";
    private static final boolean DEBUG = true;

    private SwitchPreference mEnablePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.keyboard_settings);

        mEnablePref = findPreference(SettingsUtils.KEYBOARD_ENABLE);
        if (mEnablePref == null) {
            return;
        }

        mEnablePref.setChecked(
                SettingsUtils.isSettingEnabled(getContext(), SettingsUtils.KEYBOARD_ENABLE));
        mEnablePref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (Boolean) newValue;
            if (DEBUG) Log.d(TAG, "keyboard_enable -> " + enabled);
            SettingsUtils.setSettingEnabled(getContext(), SettingsUtils.KEYBOARD_ENABLE, enabled);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateConnectedSummary();
    }

    private void updateConnectedSummary() {
        if (mEnablePref == null) {
            return;
        }
        boolean connected = KeyboardUtils.isKeyboardConnected();
        if (DEBUG) Log.d(TAG, "keyboard connected: " + connected);
        mEnablePref.setSummary(getString(
                connected ? R.string.keyboard_connected : R.string.keyboard_disconnected));
    }
}
